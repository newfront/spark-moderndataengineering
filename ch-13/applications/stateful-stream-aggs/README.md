# Chapter 13 : Aggregations and Arbitrary Stateful Structured Streaming

## Installing SBT
SBT is a scala build tool. You can download it from [scala-sbt.org](https://www.scala-sbt.org/)

Installing `sbt` can be done easily with Homebrew if you have it installed: `brew install sbt`

## 1. Building the Application
The application can be built using `SBT`, `Java 11` and `Scala 2.12`. *Note: You should have installed `Scala 2.12` and `Java 11` during the installation exercise from Chapter 2*.

~~~
sbt clean assembly
~~~

## 2. Build the Application Docker Container
Building off of the Dockerfile will produce a Docker container extending the `apache-spark-base` image from [dockerhub](https://hub.docker.com/repository/docker/newfrontdocker/apache-spark-base).
Your Spark application jar will be assembled into *fat* `jar` and copied into the `/opt/spark/app/` path within your new docker container.
~~~
docker build . -t `whoami`/spark-stateful-stream-aggs-app:latest
~~~

# Running the Exercise 1 Application 
Exercise 1 covers building the `SparkStatefulAggregationsApp`. The application will take a stream 
of redis records (`CoffeeOrder`s) and create a grouped aggregation by window over time.

~~~
df
  .groupBy($"storeId", groupingWindow())
  .agg(
    count($"orderId") as "totalOrders",
    sum($"numItems") as "totalItems",
    sum($"price") as "totalRevenue",
    avg($"numItems") as "averageNumItems"
  )
~~~

This simple aggregation will continue to process new data as it arrives, but only emit a final
record (per grouping set (`store_id`, `window(start.time,end.time)`)) when the `OutputMode` conditions
have been met. In this case, the `StreamingQuery` is monitoring the `timestamp` column across
each new record as it is being `read` into `Spark` through the `DataStreamReader`.

~~~

~~~

After building





# Make Changes and Rebuild the App
When you are working on a new application you will be working in cycles. 
1. writing the app
2. testing
3. compiling
4. wrapping in a container and doing local tests
5. release to other environments

~~~
sbt clean assembly && 
  docker build . -t `whoami`/spark-stateful-stream-aggs-app:latest
~~~

### Send Events to be processed by the app
From the `docker exec -it redis redis-cli` enter the following.
~~~
127.0.0.1:6379> xadd com:coffeeco:coffee:v1:orders MAXLEN ~ 3000 * timestamp 1625766472303 orderId ord123 storeId st1 customerId ca123 numItems 4 price 30.00
~~~
