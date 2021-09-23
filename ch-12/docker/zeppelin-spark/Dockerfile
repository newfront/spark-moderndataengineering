ARG spark_image=spark-3.1.2-jre-11-scala-2.12
FROM newfrontdocker/apache-spark-base:${spark_image} as spark

# create the base image to run Zeppelin off of
FROM apache/zeppelin:0.9.0

LABEL description='Apache Zeppelin image with Spark 3.1.2'

# copy over the custom Spark image contents
COPY --from=spark /opt/spark /opt/spark
# copy java 11 over (use to run with spark)
COPY --from=spark /usr/local/openjdk-11 /opt/openjdk-11

# note: this hive-site.xml can be mounted on demand to change the username/password/db location
COPY ./spark/conf/hive-site.xml /opt/spark/conf/

COPY ./spark/conf/spark-defaults.conf /opt/spark/conf/
COPY ./spark/jars /opt/spark/user_jars/
# fixes a bug with the zeppelin class loader and java11
COPY ./spark/jars/mariadb-java-client-2.7.2.jar /opt/spark/jars/

# copy over the overrides. This supplies zeppelin with the jre11 location and turns off compatibility checks with Spark 3x for 0.9.0 version
COPY ./conf/interpreter.json /zeppelin/conf/
COPY ./conf/zeppelin-env.sh /zeppelin/conf/

# note: please use a different user if you take this to production
USER root
