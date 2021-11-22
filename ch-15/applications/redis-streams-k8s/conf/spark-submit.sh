#!/usr/bin/env bash

K8S_MASTER=${K8S_MASTER:-k8s://https://kubernetes.default:8443}
K8S_NAMESPACE=${K8S_NAMESPACE:-spark-apps}
K8S_SERVICE_ACCOUNT_NAME=${K8S_SERVICE_ACCOUNT_NAME:-spark-controller}
SPARK_APP_NAME=${SPARK_APP_NAME:-redis-streams-app}
SPARK_APP_VERSION=${SPARK_APP_VERSION:-1.0.3}
SPARK_CONTAINER_IMAGE=${SPARK_CONTAINER_IMAGE:-mde/redis-streams-k8s:1.0.3}
SPARK_MAIN_CLASS=${SPARK_MAIN_CLASS:-com.coffeeco.data.SparkStatefulAggregationsApp}
SPARK_JAR=${SPARK_JAR:-/opt/spark/app/jars/redis-streams-k8s.jar}
SPARK_APP_ARGS=${SPARK_APP_ARGS:-}
S3_BASE_PATH=${S3_BASE_PATH:-s3a://com.coffeeco.data}

$SPARK_HOME/bin/spark-submit \
    --master ${K8S_MASTER} \
    --deploy-mode cluster \
    --name ${SPARK_APP_NAME} \
    --verbose \
    --class ${SPARK_MAIN_CLASS} \
    --conf "spark.redis.host=${REDIS_SERVICE_HOST}" \
    --conf "spark.redis.port=${REDIS_SERVICE_PORT}" \
    --conf "spark.hadoop.fs.s3a.endpoint=${MINIO_SERVICE_HOST}:${MINIO_SERVICE_PORT}" \
    --conf "spark.hadoop.fs.s3a.access.key=${MINIO_ACCESS_KEY}" \
    --conf "spark.hadoop.fs.s3a.secret.key=${MINIO_SECRET_KEY}" \
    --conf "spark.hadoop.fs.s3a.block.size=512M" \
    --conf "spark.hadoop.fs.s3a.connection.ssl.enabled=false" \
    --conf "spark.driver.extraClassPath=/opt/spark/app/user_jars/mariadb-java-client-2.7.2.jar" \
    --conf "spark.executor.extraClassPath=/opt/spark/app/user_jars/mariadb-java-client-2.7.2.jar" \
    --conf "spark.driver.extraJavaOptions=-Dconfig.file=/opt/spark/app/conf/df-grouped-aggs.conf" \
    --conf "spark.executor.extraJavaOptions=-Dconfig.file=/opt/spark/app/conf/df-grouped-aggs.conf" \
    --conf spark.kubernetes.context=${K8S_NAMESPACE} \
    --conf "spark.sql.shuffle.partitions=32" \
    --conf spark.port.maxRetries=4 \
    --conf spark.kubernetes.driver.pod.name=${SPARK_APP_NAME}-driver \
    --conf spark.kubernetes.namespace=${K8S_NAMESPACE} \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=${K8S_SERVICE_ACCOUNT_NAME} \
    --conf spark.history.fs.inProgressOptimization.enabled=true \
    --conf spark.history.fs.update.interval=5s \
    --conf spark.kubernetes.container.image=${SPARK_CONTAINER_IMAGE} \
    --conf spark.kubernetes.container.image.pullPolicy=IfNotPresent \
    --conf spark.dynamicAllocation.enabled=true \
    --conf spark.dynamicAllocation.shuffleTracking.enabled=true \
    --conf spark.dynamicAllocation.sustainedSchedulerBacklogTimeout=15s \
    --conf spark.dynamicAllocation.executorIdleTimeout=120s \
    --conf spark.kubernetes.driver.request.cores=500m \
    --conf spark.kubernetes.driver.limit.cores=1 \
    --conf spark.eventLog.enabled=true \
    --conf spark.eventLog.dir=${S3_BASE_PATH}/logs/ \
    --conf spark.history.fs.driverlog.cleaner.enabled=false \
    --conf spark.driver.log.persistToDfs.enabled=false \
    --conf spark.driver.log.dfsDir=${S3_BASE_PATH}/spark/apps/${SPARK_APP_NAME}/driver/ \
    --conf spark.driver.log.allowErasureCoding=false \
    --conf spark.driver.memory=1g \
    --conf spark.kubernetes.executor.request.cores=500m \
    --conf spark.kubernetes.executor.limit.cores=1 \
    --conf spark.dynamicAllocation.maxExecutors=4 \
    --conf spark.dynamicAllocation.executorAllocationRatio=0.25 \
    --conf spark.dynamicAllocation.minExecutors=2 \
    --conf spark.executor.memory=2g \
    --conf spark.hadoop.fs.s3a.impl=org.apache.hadoop.fs.s3a.S3AFileSystem \
    --conf spark.hadoop.fs.s3a.fast.upload=true \
    --conf spark.sql.warehouse.dir=${S3_BASE_PATH}/warehouse \
    --conf spark.scheduler.mode=FAIR \
    --conf spark.app.sink.options.checkpointLocation=${S3_BASE_PATH}/apps/spark-redis-streams-app/${SPARK_APP_VERSION} \
    --conf spark.app.sink.options.path=${S3_BASE_PATH}/silver/coffee_order_aggs
    $SPARK_JAR
