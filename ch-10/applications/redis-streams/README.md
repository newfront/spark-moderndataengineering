# Chapter 10 : RedisStreams and Structured Streaming (CoffeeCo Order Stream)

## Installing SBT
SBT is a scala build tool. You can download it from [scala-sbt.org](https://www.scala-sbt.org/)

Installing `sbt` can be done easily with Homebrew if you have it installed: `brew install sbt`

## 1. Building the Application
The application can be built using SBT, Java 11 and Scala 2.12.
You should have installed Scala 2.12 and Java 11 per the installation exercise from Chapter 2.

~~~
sbt clean assembly
~~~

## 2. Wrapping with Docker
This will produce a Docker container that extends `apache-spark-base` image from [dockerhub](https://hub.docker.com/repository/docker/newfrontdocker/apache-spark-base).
This will place the assembled *fat* `jar` into the `/opt/spark/app/` path within the docker container.
~~~
docker build . -t `whoami`/spark-redis-streams:latest
~~~

## Running the example in Container (2 steps)
This will let you test that the Docker container layout is correct. That you have placed jars in the right place.

### Start the Docker Instance in Bash Mode
**Important**

Pay close attention to the `-v` volume declarations. These are volumes are mapped to your local `~/dataengineering/` user directory. The `/spark/conf` directory contains your `hive-site.xml` as well as the `spark-defaults.conf`. While the `/spark/jars/` contains the `mariadb` driver jar for the `hive metastore`.

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

## Run the Simple Application (no checkpointing or trigger)
Exercise 1/2 use the following command to run the application.

~~~
docker run \
  -p 4040:4040 \
  --hostname spark-redis-test \
  --network mde \
  -v ~/dataengineering/spark/conf:/opt/spark/conf \
  -v ~/dataengineering/spark/jars:/opt/spark/user_jars \
  -it `whoami`/spark-redis-streams:latest \
  /opt/spark/bin/spark-submit \
  --master "local[*]" \
  --class "com.coffeeco.data.SparkRedisStreamsApp" \
  --deploy-mode "client" \
  --jars /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --driver-class-path /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --conf "spark.sql.warehouse.dir=s3a://com.coffeeco.data/warehouse" \
  --conf "spark.app.source.stream=com:coffeeco:coffee:v1:orders" \
  /opt/spark/app/spark-redis-streams.jar
~~~

### Run the Stateful Application: StatefulSparkRedisStreamsApp
Exercise 3 added checkpointing to the application. Use the following command to fire up the application.

Then when you are ready. Spin up this next app.
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
  --class "com.coffeeco.data.StatefulSparkRedisStreamsApp" \
  --deploy-mode "client" \
  --jars /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --driver-class-path /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --conf "spark.sql.warehouse.dir=s3a://com.coffeeco.data/warehouse" \
  --conf "spark.app.source.stream=com:coffeeco:coffee:v1:orders" \
  --conf "spark.app.checkpoint.location=s3a://com.coffeeco.data/apps/spark-redis-streams-app/1.0.0/simple" \
  /opt/spark/app/spark-redis-streams.jar
~~~

### Triggering: TriggeredStatefulSparkRedisStreamsApp
Exercise 4 introduced you to the concept of Triggering.

When using the `Trigger.Once` trigger, you will want to have some data in the redis stream.
`xadd com:coffeeco:coffee:v1:orders  * timestamp 1625766472513 orderId ord126 storeId st1 customerId ca183 numItems 6 price 48.00`
Just copy and change some of the data here to create some different orders or prices etc

Then when you are ready. Spin up this next app.
The difference here is the application is set to trigger once and stop.

#### Trigger.Once
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
  --class "com.coffeeco.data.TriggeredStatefulSparkRedisStreamsApp" \
  --deploy-mode "client" \
  --jars /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --driver-class-path /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --conf "spark.sql.hive.javax.jdo.option.ConnectionDriverName=org.mariadb.jdbc.Driver" \
  --conf "spark.sql.warehouse.dir=s3a://com.coffeeco.data/warehouse" \
  --conf "spark.app.source.stream=com:coffeeco:coffee:v1:orders" \
  --conf "spark.app.stream.trigger.enabled=true" \
  --conf "spark.app.stream.trigger.type=once" \
  --conf "spark.app.checkpoint.location=s3a://com.coffeeco.data/apps/spark-redis-streams-app/canary/" \
  --conf "spark.app.streaming.sink.path=s3a://com.coffeeco.data/tables/coffee.order.events/" \
  --conf "spark.app.streaming.table.name=coffee_orders" \
  /opt/spark/app/spark-redis-streams.jar
~~~

#### Trigger.ProcessingTime
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
  --class "com.coffeeco.data.TriggeredStatefulSparkRedisStreamsApp" \
  --deploy-mode "client" \
  --jars /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --driver-class-path /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --conf "spark.sql.warehouse.dir=s3a://com.coffeeco.data/warehouse" \
  --conf "spark.app.source.stream=com:coffeeco:coffee:v1:orders" \
  --conf "spark.app.stream.trigger.enabled=true" \
  --conf "spark.app.stream.trigger.type=processing" \
  --conf "spark.app.checkpoint.location=s3a://com.coffeeco.data/apps/spark-redis-streams-app/canary/" \
  --conf "spark.app.streaming.sink.path=s3a://com.coffeeco.data/tables/other.coffee.order.events/" \
  --conf "spark.app.streaming.table.name=other_coffee_orders" \
  /opt/spark/app/spark-redis-streams.jar
~~~

#### Trigger.Continuous
- This won't work unfortunately since continuous mode only works with Kafka as the source.
- But it can help you learn what exceptions you receive and how to figure out what is needed to get things running.
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
  --class "com.coffeeco.data.TriggeredStatefulSparkRedisStreamsApp" \
  --deploy-mode "client" \
  --jars /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --driver-class-path /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --conf "spark.sql.warehouse.dir=s3a://com.coffeeco.data/warehouse" \
  --conf "spark.app.source.stream=com:coffeeco:coffee:v1:orders" \
  --conf "spark.app.stream.trigger.enabled=true" \
  --conf "spark.app.stream.trigger.type=continuous" \
  --conf "spark.app.checkpoint.location=s3a://com.coffeeco.data/apps/spark-redis-streams-app/canary/" \
  --conf "spark.app.streaming.sink.path=s3a://com.coffeeco.data/tables/cont.coffee.order.events/" \
  --conf "spark.app.streaming.table.name=cont_coffee_orders" \
  /opt/spark/app/spark-redis-streams.jar
~~~

#### Streaming Output
~~~
You should see something like this come through in the console
-------------------------------------------
Batch: 0
-------------------------------------------
+-------------+-------+-------+----------+--------+-----+
|    timestamp|orderId|storeId|customerId|numItems|price|
+-------------+-------+-------+----------+--------+-----+
|1625766472513| ord126|    st1|     ca183|       6| 48.0|
|1625766472503| ord124|    st1|     ca153|       1| 10.0|
|1625766472513| ord125|    st1|     ca173|       2|  7.0|
+-------------+-------+-------+----------+--------+-----+
This shows that you have read 3 of the entries in the stream.

Now if you add 1 more entry and read from the stream again.
you will get only 1 additional elements since these first 3 items have already been viewed.
-------------------------------------------
Batch: 1
-------------------------------------------
+-------------+-------+-------+----------+--------+-----+
|    timestamp|orderId|storeId|customerId|numItems|price|
+-------------+-------+-------+----------+--------+-----+
|1625766472513| ord777|    st2|     ca663|      16|448.0|
+-------------+-------+-------+----------+--------+-----+
This is not the best way to throttle an applciation. But it can be useful tactic when testing that things are 
actually working as expected. 
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

### Send Events to be processed by the app
From the `docker exec -it redis redis-cli` enter the following.
~~~
127.0.0.1:6379> xadd com:coffeeco:coffee:v1:orders MAXLEN ~ 3000 * timestamp 1625766472303 orderId ord123 storeId st1 customerId ca123 numItems 4 price 30.00
~~~
