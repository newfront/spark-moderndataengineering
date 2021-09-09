FROM newfrontdocker/apache-spark-base:spark-3.1.2-jre-11-scala-2.12

COPY target/scala-2.12/spark-kafka-coffee-orders-assembly-0.1-SNAPSHOT.jar /opt/spark/app/spark-kafka-coffee-orders.jar
# copy the app configs
COPY conf /opt/spark/app/conf
EXPOSE 4040