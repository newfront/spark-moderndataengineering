ARG app_jar=./target/scala-2.12/redis-streams-k8s-assembly-1.0.0-SNAPSHOT.jar
ARG spark_base_version=apache-spark-base:spark-3.1.2-jre-11-scala-2.12

FROM newfrontdocker/${spark_base_version}
ARG app_jar

USER 1000

# copy and rename
COPY --chown=1000 ${app_jar} /opt/spark/app/jars/redis-streams-k8s.jar

# copy the config for the app
COPY --chown=1000 conf /opt/spark/app/conf
COPY --chown=1000 user_jars /opt/spark/app/user_jars

EXPOSE 4040
