## Create the Spark Submit Command & Defaults ConfigMap
All of the files in the `config` directory can be stored in the Kubernetes configmap api.
~~~
kubectl create configmap spark-redis-streams-conf --from-file=config -n spark-apps
~~~
> `configmap/spark-redis-streams-conf created`
> Delete: `kubectl delete configmap/spark-redis-streams-conf -n spark-apps`

## Create the Hive Metastore Secret
Given secrets can live on the Kubernetes cluster itself. You can add your hive-site.xml directly to the secrets for a given namespace. 

Add the hive-site.xml secret to the spark-apps namespace.
~~~
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Secret
metadata:
  name: hivesite-admin
  namespace: spark-apps
  labels:
    rbac.coffeeco.auth: admin
type: Opaque
stringData:
  hive-site.xml: |
    <configuration>
      <property>
        <name>javax.jdo.option.ConnectionURL</name>
        <value>jdbc:mysql://mysql-service.data-services.svc.cluster.local:3306/metastore</value>
      </property>
      <property>
        <name>javax.jdo.option.ConnectionDriverName</name>
        <value>org.mariadb.jdbc.Driver</value>
      </property>
      <property>
        <name>javax.jdo.option.ConnectionUserName</name>
        <value>dataeng</value>
      </property>
      <property>
        <name>javax.jdo.option.ConnectionPassword</name>
        <value>dataengineering_user</value>
      </property>
    </configuration>
EOF
~~~

## add the MINIO Secrets 
~~~
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Secret
metadata:
  name: minio-access
  namespace: spark-apps
  labels:
    rbac.coffeeco.auth: admin
type: Opaque
stringData:
  access: minio
  secret: minio_admin
EOF
~~~

## Launch a Manual Jump Host

1. Deploy the manual spark-application (spark-redis-streams).
~~~
kubectl apply -f spark-on-kubernetes/spark-redis-streams/deployment-manual.yaml
~~~

2. When this new Pod comes up, then head over to the Pod.
~~~
kubectl exec --stdin --tty pod/spark-redis-streams-app -n spark-apps -- bash
~~~

3. Run the Spark Application
> In the below example, I have added the local minikube `k8s` control plane url. `kubectl cluster-info`. This will be different on your local setup.

~~~
$SPARK_HOME/bin/spark-submit \
  --master k8s://https://192.168.64.9:8443 \
  --name redis-streams-k8s \
  --deploy-mode cluster \
  --packages org.mariadb.jdbc:mariadb-java-client:2.7.2 \
  --class "com.coffeeco.data.SparkStatefulAggregationsApp" \
  --verbose \
  --conf "spark.driver.extraClassPath=/opt/spark/app/user_jars/mariadb-java-client-2.7.2.jar" \
  --conf "spark.executor.extraClassPath=/opt/spark/app/user_jars/mariadb-java-client-2.7.2.jar" \
  --conf "spark.kubernetes.driver.pod.name=${SPARK_APP_NAME}-driver" \
  --conf "spark.kubernetes.context=spark-apps" \
  --conf "spark.sql.shuffle.partitions=32" \
  --conf "spark.kubernetes.namespace=spark-apps" \
  --conf "spark.kubernetes.authenticate.driver.serviceAccountName=spark-controller" \
  --conf "spark.app.source.format=redis" \
  --conf "spark.app.source.options.stream.keys=com:coffeeco:coffee:v1:orders" \
  --conf "spark.app.source.options.stream.read.batch.size=100" \
  --conf "spark.app.source.options.stream.read.block=1000" \
  --conf "spark.redis.host=${REDIS_SERVICE_HOST}" \
  --conf "spark.redis.port=${REDIS_SERVICE_PORT}" \
  --conf "spark.hadoop.fs.s3a.endpoint=${MINIO_SERVICE_HOST}:${MINIO_SERVICE_PORT}" \
  --conf "spark.hadoop.fs.s3a.access.key=${MINIO_ACCESS_KEY}" \
  --conf "spark.hadoop.fs.s3a.secret.key=${MINIO_SECRET_KEY}" \
  --conf "spark.hadoop.fs.s3a.block.size=512M" \
  --conf "spark.hadoop.fs.s3a.connection.ssl.enabled=false" \
  --conf "spark.kubernetes.container.image=mde/redis-streams-k8s:1.0.4" \
  --conf "spark.kubernetes.container.image.pullPolicy=Never" \
  --conf "spark.dynamicAllocation.enabled=false" \
  --conf "spark.kubernetes.driver.request.cores=500m" \
  --conf "spark.kubernetes.driver.limit.cores=1" \
  --conf "spark.driver.memory=1g" \
  --conf "spark.kubernetes.executor.request.cores=500m" \
  --conf "spark.kubernetes.executor.limit.cores=1" \
  --conf "spark.executor.memory=2g" \
  --conf "spark.executor.instances=4" \
  --conf "spark.sql.warehouse.dir=s3a://com.coffeeco.data/warehouse" \
  --conf "spark.app.sink.format=parquet" \
  --conf "spark.app.sink.queryName=coffee_orders_aggs" \
  --conf "spark.app.sink.trigger.enabled=true" \
  --conf "spark.app.sink.trigger.type=process" \
  --conf "spark.app.sink.processing.interval=30 seconds" \
  --conf "spark.app.sink.outputMode=append" \
  --conf "spark.sql.streaming.checkpointLocation=s3a://com.coffeeco.data/apps/spark-redis-streams-app/1.0.0" \
  --conf "spark.app.sink.options.checkpointLocation=s3a://com.coffeeco.data/apps/spark-redis-streams-app/1.0.0" \
  --conf "spark.app.sink.options.path=s3a://com.coffeeco.data/warehouse/silver/coffee_order_aggs" \
  --conf "spark.app.sink.output.tableName=silver.coffee_order_aggs" \
  local:///opt/spark/app/jars/redis-streams-k8s.jar
~~~

## Go add some events using the Redis-Cli

1. Pop onto the Redis Pod from part1
~~~
kubectl exec -it redis-deployment-pv-849c64f7f4-dnzq6 -n data-services -- redis-cli
~~~

2. Add some records
~~~
xadd com:coffeeco:coffee:v1:orders  * timestamp 1637548381179 orderId ord123 storeId st1 customerId ca100 numItems 6 price 48.00
xadd com:coffeeco:coffee:v1:orders  * timestamp 1637548458800 orderId ord124 storeId st1 customerId ca101 numItems 3 price 36.00
~~~

### Monitor the Pods being Created, or Removed
~~~
kubectl get pods -n spark-apps --watch
~~~
> You will see the status of the driver pod and the executor pods created to run the workload along with their statuses.

### View Spark Driver Pod Logs
Viewing the logs can help when you are debugging your applications, or checking how things are going.
~~~
kubectl -n spark-apps logs spark-redis-streams-app-driver -f
~~~
> The `-f` stands for `follow`. This allows you to view the logs in real-time.

### View the Spark UI on the Driver Pod
~~~
kubectl -n spark-apps port-forward spark-redis-streams-app-driver 4040:4040
~~~
> Now you'll have the local Spark Driver UI running on your local machine

## Check Permissions
Checking in on your service account permissions. You can replace the resource to check that the role binding is correctly applied. 

```
kubectl auth can-i {verb} {resource}
```

Example:
~~~
kubectl auth can-i create configmaps \
  --namespace spark-apps \
  --as system:serviceaccount:spark-apps:spark-controller
~~~

