ARG app_jar=./target/scala-2.12/redis-streams-k8s-assembly-1.0.0-SNAPSHOT.jar
ARG spark_base_version=apache-spark-base:spark-3.1.2-jre-11-scala-2.12

FROM newfrontdocker/${spark_base_version}
ARG app_jar

# assumes the Spark Base container was built with the default spark-uid
# https://github.com/apache/spark/blob/master/resource-managers/kubernetes/docker/src/main/dockerfiles/spark/Dockerfile#L21
USER 185

# copy and rename
COPY --chown=185 ${app_jar} /opt/spark/app/jars/redis-streams-k8s.jar

# copy the config for the app
COPY --chown=185 conf /opt/spark/app/conf
COPY --chown=185 user_jars /opt/spark/app/user_jars

EXPOSE 4040
