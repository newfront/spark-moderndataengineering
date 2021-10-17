ARG app_jar=./target/scala-2.12/spark-stateful-streaming-aggs-0.1-SNAPSHOT.jar
FROM newfrontdocker/apache-spark-base:spark-3.1.2-jre-11-scala-2.12

COPY ${app_jar} /opt/spark/app/spark-stateful-streaming-aggs.jar
# copy the config for the app
COPY conf /opt/spark/app/conf

EXPOSE 4040
