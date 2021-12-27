## 1. Create the Spark Submit Command & Defaults ConfigMap
All of the files in the `config` directory can be stored in the Kubernetes configmap api.
~~~
kubectl create configmap spark-redis-streams-conf --from-file=config -n spark-apps
~~~
> `configmap/spark-redis-streams-conf created`

### Dump the Contents of a ConfigMap
~~~
kubectl get configmap/spark-redis-streams-conf -n spark-apps -o yaml
~~~
> You can use the output YAML to define resources. Just remove the creationTimestamp, resourceVersion, uid from the metadata.

### Delete a ConfigMap
~~~
kubectl delete configmap/spark-redis-streams-conf -n spark-apps
~~~

## 2. Add Secrets for the Hive MetaStore and MinIO (S3)
Given secrets can live on the Kubernetes cluster itself. You can add your hive-site.xml directly to the secrets for a given namespace. The following example shows how to safely copy a secret into your cluster. 

### The HiveSite Secret
Add the `hive-site.xml` secret to the `spark-apps` namespace by running the following.
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
        <value>jdbc:mysql://mysql-service.spark-apps.svc.cluster.local:3306/metastore</value>
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

### Add the MinIO Secret
Apply the following to add a secret encapsulating the MinIO (s3) access and secret keys.
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

## 3. Deploy the Spark Pod (spark-redis-streams) Manually.

Deploy the manual spark-application (spark-redis-streams).
~~~
kubectl apply -f spark-on-kubernetes/spark-redis-streams/deployment-manual.yaml
~~~

2. When the new Pod comes up, start an `exec` process on the new Pod via the `kubectl`.
~~~
kubectl exec --stdin --tty pod/spark-redis-streams-app -n spark-apps -- bash
~~~

3. Manually Run the Spark Application

From within the `exec` process on the Pod.
~~~
/opt/spark/app/conf/spark-submit.sh
~~~
> The spark-submit.sh executable ships with the Docker container and can be found in `/ch-15/applications/redis-streams-k8s/conf/spark-submit.sh`

> Tip: `bash /opt/spark/app/conf/spark-submit-lite.sh` runs a minimal version of the spark-submit command, leaning on the spark-defaults.conf from the `spark-redis-streams-app` launch Pod. For production deployments a key to your success is idempotent deployments, so if a value isn't part of the `deployment-*.yaml` (included environment variables, and configMaps) then it shouldn't be something you rely on.

## 4. Trigger CoffeeOrder Processing with the RedisCli

1. Pop onto the Redis Pod from part1
~~~
kubectl exec -it redis-deployment-pv-849c64f7f4-dnzq6 -n data-services -- redis-cli
~~~

2. Add some records
~~~
xadd com:coffeeco:coffee:v1:orders  * timestamp 1637548381179 orderId ord123 storeId st1 customerId ca100 numItems 6 price 48.00
xadd com:coffeeco:coffee:v1:orders  * timestamp 1637548458800 orderId ord124 storeId st1 customerId ca101 numItems 3 price 36.00
xadd com:coffeeco:coffee:v1:orders  * timestamp 1637720977241 orderId ord125 storeId st2 customerId ca102 numItems 1 price 4.99
xadd com:coffeeco:coffee:v1:orders  * timestamp 1637783601557 orderId ord126 storeId st1 customerId ca103 numItems 2 price 10.99
~~~

## 5. Monitor the Pod Statuses in the Namespace
~~~
kubectl get pods -n spark-apps --watch
~~~
> You will see the status of the driver pod and the executor pods created to run the workload along with their statuses.

## 6. View Spark Driver Pod Logs
Viewing the logs can help when you are debugging your applications, or checking how things are going.
~~~
kubectl -n spark-apps logs spark-redis-streams-app-driver -f
~~~
> The `-f` stands for `follow`. This allows you to view the logs in real-time.

## 7. Forward the Spark UI from the Driver Pod to your Local Machine
~~~
kubectl -n spark-apps port-forward spark-redis-streams-app-driver 4040:4040
~~~
> Now you'll have the local Spark Driver UI running on your local machine

## 8. Automating the Full Deployment
~~~
kubectl apply -f deployment-redis-streams.yaml
~~~

```
service/redis-service unchanged
configmap/spark-redis-streams-conf unchanged
secret/hivesite-admin configured
secret/minio-access configured
deployment.apps/spark-redis-streams-app created
```

~~~
kubectl get pods -n spark-apps --watch
~~~

```
NAME                                              READY   STATUS    RESTARTS   AGE
spark-redis-streams-app-758c4df9ff-ftxkz          1/1     Running   0          20s
spark-redis-streams-app-758c4df9ff-ftxkz-driver   1/1     Running   0          11s
```

Now your spark application brings all of its own resources with it.

> Look at the tool called Kustomize which ships with the `kubectl`. This can automatically render complete deployments like the one from the `deployment-redis-streams.yaml`. Kustomize is beyond the scope of the book but can help solve the problems.

# Trouble Shooting
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

## Service Accounts
~~~
kubectl get sa spark-controller -n spark-apps -o yaml
~~~

## View the Spark Application and Pods
```
kubectl get pods -n spark-apps
```

**Response**
~~~
NAME                                                  READY   STATUS    RESTARTS   AGE
spark-redis-streams-app                               1/1     Running   0          5m27s
redis-streams-app-driver                              1/1     Running   0          3m45s
spark-redis-stream-aggs-app-99b48f7d4f7aaf33-exec-1   1/1     Running   0          3m34s
spark-redis-stream-aggs-app-99b48f7d4f7aaf33-exec-2   1/1     Running   0          3m33s
~~~