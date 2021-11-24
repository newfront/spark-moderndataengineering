#!/usr/bin/env bash

# Use % kubectl cluster-info to get the control plane URL.
K8S_MASTER=${K8S_MASTER:-k8s://https://${KUBERNETES_SERVICE_HOST}:${KUBERNETES_SERVICE_PORT_HTTPS}}
K8S_NAMESPACE=${K8S_NAMESPACE:-spark-apps}
SPARK_EVENT_LOG_ENABLED=${SPARK_EVENT_LOG_ENABLED:-false}
K8S_SERVICE_ACCOUNT_NAME=${K8S_SERVICE_ACCOUNT_NAME:-spark-controller}
SPARK_APP_NAME=${SPARK_APP_NAME:-redis-streams-app}
SPARK_APP_VERSION=${SPARK_APP_VERSION:-1.0.0}
SPARK_DYNAMIC_ALLOCATION_ENABLED=${SPARK_DYNAMIC_ALLOCATION_ENABLED:-false}
SPARK_CONTAINER_IMAGE=${SPARK_CONTAINER_IMAGE:-mde/redis-streams-k8s:1.0.0}
SPARK_CONTAINER_PULL_POLICY=${SPARK_CONTAINER_PULL_POLICY:-IfNotPresent}
SPARK_MAIN_CLASS=${SPARK_MAIN_CLASS:-com.coffeeco.data.SparkStatefulAggregationsApp}
SPARK_JAR=${SPARK_JAR:-local:///opt/spark/app/jars/redis-streams-k8s.jar}
S3_BASE_PATH=${S3_BASE_PATH:-s3a://com.coffeeco.data}

$SPARK_HOME/bin/spark-submit \
  --verbose \
  --master ${K8S_MASTER} \
  --name redis-streams-k8s \
  --deploy-mode cluster \
  --jars "local:///opt/spark/app/user_jars/mariadb-java-client-2.7.2.jar" \
  --class "com.coffeeco.data.SparkStatefulAggregationsApp" \
  --conf "spark.driver.extraClassPath=/opt/spark/app/user_jars/mariadb-java-client-2.7.2.jar" \
  --conf "spark.executor.extraClassPath=/opt/spark/app/user_jars/mariadb-java-client-2.7.2.jar" \
  --conf "spark.kubernetes.driver.pod.name=${SPARK_APP_NAME}-driver" \
  --conf "spark.kubernetes.context=${K8S_NAMESPACE}" \
  --conf "spark.sql.shuffle.partitions=32" \
  --conf "spark.kubernetes.namespace=${K8S_NAMESPACE}" \
  --conf "spark.kubernetes.authenticate.driver.serviceAccountName=${K8S_SERVICE_ACCOUNT_NAME}" \
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
  --conf "spark.kubernetes.container.image=${SPARK_CONTAINER_IMAGE}" \
  --conf "spark.kubernetes.container.image.pullPolicy=${SPARK_CONTAINER_PULL_POLICY}" \
  --conf "spark.dynamicAllocation.enabled=${SPARK_DYNAMIC_ALLOCATION_ENABLED}" \
  --conf "spark.kubernetes.driver.request.cores=500m" \
  --conf "spark.kubernetes.driver.limit.cores=1" \
  --conf "spark.driver.memory=1g" \
  --conf "spark.kubernetes.executor.request.cores=500m" \
  --conf "spark.kubernetes.executor.limit.cores=1" \
  --conf "spark.executor.memory=2g" \
  --conf "spark.executor.instances=4" \
  --conf "spark.sql.warehouse.dir=${S3_BASE_PATH}/warehouse" \
  --conf "spark.app.sink.format=parquet" \
  --conf "spark.app.sink.queryName=coffee_orders_aggs" \
  --conf "spark.app.sink.trigger.enabled=true" \
  --conf "spark.app.sink.trigger.type=process" \
  --conf "spark.app.sink.processing.interval=30 seconds" \
  --conf "spark.app.sink.outputMode=append" \
  --conf "spark.sql.streaming.checkpointLocation=${S3_BASE_PATH}/apps/spark-redis-streams-app/1.0.0" \
  --conf "spark.app.sink.options.checkpointLocation=${S3_BASE_PATH}/apps/spark-redis-streams-app/1.0.0" \
  --conf "spark.app.sink.options.path=${S3_BASE_PATH}/warehouse/silver/coffee_order_aggs" \
  --conf "spark.app.sink.output.tableName=silver.coffee_order_aggs" \
  local:///opt/spark/app/jars/redis-streams-k8s.jar