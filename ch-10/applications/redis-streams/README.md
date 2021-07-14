# Chapter 10 : RedisStreams and Structured Streaming (CoffeeCo Order Stream)

## Installing SBT
SBT is a scala build tool. You can download it from [scala-sbt.org](https://www.scala-sbt.org/)

Installing `sbt` can be done easily with Homebrew if you have it installed: `brew install sbt`

## Building the Application
The application can be built using SBT, Java 11 and Scala 2.12.
You should have installed Scala 2.12 and Java 11 per the exercise from Chapter 2. 

~~~
sbt clean assembly
~~~

## Wrapping with Docker
This will produce a Docker container that extends `apache-spark-base` image from [dockerhub](https://hub.docker.com/repository/docker/newfrontdocker/apache-spark-base).
This will place the assembled *fat* `jar` into the `/opt/spark/app/` path within the docker container.
~~~
docker build . -t `whoami`/spark-redis-streams:latest
~~~

## Running the example in Container (2 steps)
This will let you test that the Docker container layout is correct. That you have placed jars in the right place.

### Start the Docker Instance in Bash Mode
~~~
docker run \
  -p 4040:4040 \
  --hostname spark-redis-test \
  --network mde \
  -v /Users/`whoami`/dataengineering/spark/conf:/opt/spark/conf \
  -v /Users/`whoami`/dataengineering/spark/jars:/opt/spark/user_jars \
  -it `whoami`/spark-redis-streams:latest \
  bash
~~~

### Manually Trigger the Spark Submit
~~~
$SPARK_HOME/bin/spark-submit \
  --master "local[*]" \
  --class "com.coffeeco.data.SparkRedisStreamsApp" \
  --deploy-mode "client" \
  --jars /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --driver-class-path /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --conf "spark.sql.hive.javax.jdo.option.ConnectionDriverName=org.mariadb.jdbc.Driver" \
  --conf "spark.sql.warehouse.dir=s3a://com.coffeeco.data/warehouse" \
  --conf "spark.app.source.stream=com:coffeeco:coffee:v1:orders" \
  /opt/spark/app/spark-redis-streams.jar
~~~

### Make Changes and Rebuild the App
When you are working on a new application you will be working in cycles. 
1. writing the app
2. testing
3. compiling
4. wrapping in a container and doing local tests
5. release to other environments

~~~
sbt clean assembly && docker build . -t `whoami`/spark-redis-streams:latest 
~~~

### Running the Local Structured Streaming App
~~~
docker run \
  -p 4040:4040 \
  --hostname spark-redis-test \
  --network mde \
  -v /Users/`whoami`/dataengineering/spark/conf:/opt/spark/conf \
  -v /Users/`whoami`/dataengineering/spark/jars:/opt/spark/user_jars \
  -it `whoami`/spark-redis-streams:latest \
  /opt/spark/bin/spark-submit \
  --master "local[*]" \
  --class "com.coffeeco.data.SparkRedisStreamsApp" \
  --deploy-mode "client" \
  --jars /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --driver-class-path /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --conf "spark.sql.hive.javax.jdo.option.ConnectionDriverName=org.mariadb.jdbc.Driver" \
  --conf "spark.sql.warehouse.dir=s3a://com.coffeeco.data/warehouse" \
  --conf "spark.app.source.stream=com:coffeeco:coffee:v1:orders" \
  /opt/spark/app/spark-redis-streams.jar
~~~
