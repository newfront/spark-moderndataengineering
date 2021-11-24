# Chapter 15 : Part2: Spark on Kubernetes

## Build the Application and Use the Minikube Docker Environment for Local Containers

### Step 1: Build the Application
~~~
sbt clean assembly
~~~

### Step 2: Create the Container
In order to use your container within your local `minikube` environment, you will have to first update your Docker env.

You can do this by simply applying the environment information using `minikube docker-env`. 
Then simply build the application image to push the resulting container into the `minikube docker context`. 

~~~
eval $(minikube docker-env)
docker build . -t mde/redis-streams-k8s:1.0.0
~~~

Now the container will be available to run via Spark Submit.

### Step 3: Running on Kubernetes
Assuming you have bootstrapped your environment following the `intro-to-kubernetes` and `spark-on-kubernetes` README's, the final step is to submit our application.

See `ch-15/spark-on-kubernetes/spark-redis-streams/README.md` for the complete step-by-step details. 

### Manually Run the Application
After launching your Spark Deployment Pod: `kubectl apply -f spark-on-kubernetes/deployment-manual.yaml`, just open up an `exec` session to go ahead and run the launch command.
```
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
  --conf "spark.kubernetes.container.image=mde/redis-streams-k8s:1.0.0" \
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
```
