# Exercise 2: Lessons on Interoperability: Generating CoffeeOrders with Protobuf and Spark
This exercise is an introduction to using binary structured data within the Apache Spark ecosystem.
For additional information related to using Kafka and Spark outside of the scope of the book, visit [The Official Spark Kafka Integration Guide](http://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html).

**Note: Running Kafka**

The local Kafka cluster needs to be running in order to compile the code.
See `ch-11/docker/kadka/README.md` to get things running.

**Data Formats: Google Protocol Buffers**

This exercise uses [Google Protocol Buffers (protobuf)](https://developers.google.com/protocol-buffers).
Protobuf is a serializable structured-data format, like Apache Avro, that provides a mechanism for exchanging data between platforms and programming languages to achieve data interoperability between systems and services.    

If you want to make changes to the `/data/protobuf/coffee.common.` you will have to recompile the protobuf definitions.
See the `README.md` under `/data/protobuf/` to recompile or add any new definitions.

## 1. Build the Application
The application can be built using SBT, Java 11 and Scala 2.12.
You should have installed Scala 2.12 and Java 11 per the installation exercise from Chapter 2.

~~~
// build the application and run the tests (requires Kafka to be running)
sbt clean assembly

// skip the tests and just build already (doesn't require kafka to be running)
sbt 'set assembly / test := {}' clean assembly
~~~

## 2. Generate the Docker Container
This will produce a Docker container that extends `apache-spark-base` image from [dockerhub](https://hub.docker.com/repository/docker/newfrontdocker/apache-spark-base).
This will place the assembled *fat* `jar` into the `/opt/spark/app/` path within the docker container.
~~~
docker build . -t `whoami`/spark-kafka-coffee-orders:latest
~~~


## Producing Streams of Binary Structured Data with Spark
To view the end to end example, you will need to have 2 terminal windows open. The first terminal window will be running the Kafka 
console consumer. The `kafka-console-consumer` is a shell application that ships out-of-the-box with Kafka that prints in near real-time, 
the records you publish to Kafka. The second window is where you will actually run the `CoffeeOrder` generator.

**1. Start up the Kafka Console Consumer**
~~~
docker exec -it kafka_kafka-0_1 \
  /opt/bitnami/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka_0:9092,kafka_1:9092,kafka_2:9092 \
  --topic com.coffeeco.coffee.v1.orders
~~~

**2. Run the CoffeeOrder Event Generator**
~~~
docker run \
  -p 4040:4040 \
  --hostname spark-kafka \
  --network mde \
  -v ~/dataengineering/spark/conf:/opt/spark/conf \
  -v ~/dataengineering/spark/jars:/opt/spark/user_jars \
  -it `whoami`/spark-kafka-coffee-orders:latest \
  /opt/spark/bin/spark-submit \
  --master "local[*]" \
  --class "com.coffeeco.data.generators.CoffeeOrderGenerator" \
  --deploy-mode "client" \
  --jars /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --driver-class-path /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --conf "spark.sql.warehouse.dir=s3a://com.coffeeco.data/warehouse" \
  --conf "spark.app.sink.option.kafka.bootstrap.servers=kafka_0:9092,kafka_1:9092,kafka_2:9092" \
  --conf "spark.app.sink.kafka.topic=com.coffeeco.coffee.v1.orders" \
  --conf "spark.data.generator.totalRecords=100" \
  --conf "spark.data.generator.indexOffset=0" \
  /opt/spark/app/spark-kafka-coffee-orders.jar
~~~

# Exercise 3: Consuming Streams of Binary Structured Data with Spark
Following in the footsteps of Exercise 2, you'll learn to consume serializable structured data with Apache Spark.
*I like to refer to this as consuming or producing Protocol Streams, as the data flowing through the system has a defined protocol.*
You'll be using the application framework from Exercise 2, reusing the techniques that have been built up across the book.
You'll be reusing the `SparkApplication` trait, `triggerEnabled` and `triggerType` as well as the new configurations from this chapter.

See the `conf/coffee_orders_consumer.conf` for details on all of the configurations used in this example.

**Running with Basic Overrides**
~~~
docker run \
  -p 4040:4040 \
  --hostname spark-kafka-coffee-consumer \
  --network mde \
  -v ~/dataengineering/spark/conf/hive-site.xml:/opt/spark/conf/hive-site.xml \
  -v ~/dataengineering/spark/jars:/opt/spark/user_jars \
  -it `whoami`/spark-kafka-coffee-orders:latest \
  /opt/spark/bin/spark-submit \
  --master "local[*]" \
  --class "com.coffeeco.data.SparkKafkaCoffeeOrdersApp" \
  --deploy-mode "client" \
  --jars /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --driver-class-path /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --driver-java-options "-Dconfig.file=/opt/spark/app/conf/coffee_orders_consumer.conf" \
  --conf "spark.sql.streaming.kafka.useDeprecatedOffsetFetching=false" \
  --conf "spark.app.source.option.startingOffsets=earliest" \
  --conf "spark.app.stream.trigger.type=once" \
  /opt/spark/app/spark-kafka-coffee-orders.jar
~~~

**Running with Full Set of Options**
~~~
docker run \
  -p 4040:4040 \
  --hostname spark-kafka-coffee-consumer \
  --network mde \
  -v ~/dataengineering/spark/conf/hive-site.xml:/opt/spark/conf/hive-site.xml \
  -v ~/dataengineering/spark/jars:/opt/spark/user_jars \
  -it `whoami`/spark-kafka-coffee-orders:latest \
  /opt/spark/bin/spark-submit \
  --master "local[*]" \
  --class "com.coffeeco.data.SparkKafkaCoffeeOrdersApp" \
  --deploy-mode "client" \
  --jars /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --driver-class-path /opt/spark/user_jars/mariadb-java-client-2.7.2.jar \
  --driver-java-options "-Dconfig.file=/opt/spark/app/conf/coffee_orders_consumer.conf" \
  --conf "spark.sql.warehouse.dir=s3a://com.coffeeco.data/warehouse" \
  --conf "spark.sql.streaming.kafka.useDeprecatedOffsetFetching=false" \
  --conf "spark.app.source.format=kafka" \
  --conf "spark.app.source.option.subscribe=com.coffeeco.coffee.v1.orders" \
  --conf "spark.app.source.option.kafka.bootstrap.servers=kafka_0:9092,kafka_1:9092,kafka_2:9092" \
  --conf "spark.app.source.option.startingOffsets=earliest" \
  --conf "spark.app.source.options.maxOffsetsPerTrigger=1000" \
  --conf "spark.app.source.options.minPartitions=8" \
  --conf "spark.app.source.options.failOnDataLoss=false" \
  --conf "spark.app.source.option.includeHeaders=false" \
  --conf "spark.app.sink.format=parquet" \
  --conf "spark.app.sink.queryName=coffee_orders_consumer" \
  --conf "spark.app.sink.option.checkpointLocation=s3a://com.coffeeco.data/apps/spark-kafka-coffee-orders-app/1.0.0/" \
  --conf "spark.app.sink.option.path=s3a://com.coffeeco.data/warehouse/silver/coffee_orders" \
  --conf "spark.app.sink.output.tableName=silver.coffee_orders" \
  --conf "spark.app.stream.trigger.enabled=true" \
  --conf "spark.app.stream.trigger.type=once" \
  --conf "spark.app.stream.output.mode=append" \
  --conf "spark.app.stream.processing.interval=30 seconds" \
  /opt/spark/app/spark-kafka-coffee-orders.jar
~~~