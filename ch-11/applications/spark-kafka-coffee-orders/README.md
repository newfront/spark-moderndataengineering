# Exercise 2: Lessons on Interoperability: Generating CoffeeOrders with Protobuf and Spark
This exercise is an introduction to using binary structured data within the Apache Spark ecosystem.
For additional information related to using Kafka and Spark outside of the scope of the book, visit [The Official Spark Kafka Integration Guide](http://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html).

## Note: Running Kafka
The local Kafka cluster needs to be running in order to compile the code.
See `ch-11/docker/kadka/README.md` to get things running.

## 1. Building the Application
The application can be built using SBT, Java 11 and Scala 2.12.
You should have installed Scala 2.12 and Java 11 per the installation exercise from Chapter 2.

~~~
// build the application and run the tests (requires Kafka to be running)
sbt clean assembly

// skip the tests and just build already (doesn't require kafka to be running)
sbt 'set assembly / test := {}' clean assembly
~~~

## 2. Wrapping with Docker
This will produce a Docker container that extends `apache-spark-base` image from [dockerhub](https://hub.docker.com/repository/docker/newfrontdocker/apache-spark-base).
This will place the assembled *fat* `jar` into the `/opt/spark/app/` path within the docker container.
~~~
docker build . -t `whoami`/spark-kafka-coffee-orders:latest
~~~

## Compiling the Project
From the root of the project.
~~~
cd applications/spark-kafka-coffee-orders &&
sbt clean assembly
~~~

If you want to make changes to the `/data/protobuf/coffee.common.` you will have to recompile the protobuf definitions.
See the `README.md` under `/data/protobuf/` to recompile or add any new definitions.

## Generating and Consuming CoffeeOrders with Spark

### Observing Record Production
When you are running the CoffeeOrder generator it can be interesting to consume the data from one of the available Docker based Kafka brokers to see what is happening.
~~~
docker exec -it kafka_kafka-0_1 \
  /opt/bitnami/kafka/bin/kafka-console-consumer.sh \
--bootstrap-server kafka_0:9092,kafka_1:9092,kafka_2:9092 \
--topic com.coffeeco.coffee.v1.orders
~~~