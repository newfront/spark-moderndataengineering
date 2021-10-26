**Important**
These exercises reuse the local data engineering environment setup under the path `~/dataengineering`. This exercise is reusing the shared `mysql` and `minio` mount points. See chapter 8 for a breakdown on setting up the ecosystem for wider reuse.

# Exercise 1: Using RedisStreams to test drive Structured Streaming.

## Step 1: Spin up the local environment
Start up the docker environment. This will spin up redis, minio and mysql.
~~~
docker compose \
  -f docker-compose.yaml \
  up -d
~~~

## Copy the Spark Conf and Jars directories
If you don't have the `~/dataengineering/spark/conf` or `~/dataengineering/spark/jars` directories, you can copy the contents of `/ch-10/exercises/common/spark` into your local data engineering directory.

`ls -l ~/dataengineering/spark`

> `tree ~/dataengineering/spark`

~~~
├── conf
│   ├── hive-site.xml
│   └── spark-defaults.conf
└── jars
    ├── aws-java-sdk-bundle-1.11.375.jar
    ├── hadoop-aws-3.2.0.jar
    ├── hadoop-cloud-storage-3.2.0.jar
    ├── mariadb-java-client-2.7.2.jar
    ├── mysql-connector-java-8.0.23.jar
    ├── spark-event-extractor.jar
    └── spark-redis_2.12-3.1.2.jar
~~~

## Step 2: Using the Redis Cli / Monitor
Open two new console windows. The first will attach to the redis container and act as the redis-cli (redis command line). This will be used for adding new event data into our Redis Stream. The other window will be used for monitoring redis. This is for observation reasons only to provide a glimpse into what is happening behind the scenes!

**Redis CLI**
In the first window, run the following command.
~~~
docker exec -it redis redis-cli
~~~

**Redis CLI Monitor**
In the second window, run the following command.
~~~
docker exec -it redis redis-cli monitor
~~~

## Step 3: Compile the Spark Application and Run it in the Spark Docker container
Open up `ch-10/applications/redis-stream`, and follow the instructions in the README. This will result in the docker container `spark-redis-streams:latest` which you'll see in the following command.

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

This will start the spark application and it will await any events you produce using the `redis-cli`. When you are done, exit the process and the container will shut down.

## Step 4: Clean up the processes when you are done
~~~
docker compose \
  -f docker-compose.yaml \
  down --remove-orphans
~~~

# Redis Primer
If you don't use redis on a daily basis that is okay. It is best known as a **key/value** store but did you know it is in fact an extreamly performant data structure store? Redis has a growing list of capabilities, but the basics will get you far.

## Set and Get a Key with Expiry
We will use the basic KEY api to set a key with a name. Given each key can exist in memory forever, and that we don't have unlimited resources, it is standard practice to give each key an `expiry`. This value is represented in seconds and Redis will handle the performant eviction of its own keys. You can add more time, or adjust the expiry of a key once it has been created as well. This expiry is also known as the keys time to live (TTL).

### Set
`<set key value EX seconds>`
~~~
127.0.0.1:6379> set name scott EX 30
OK
~~~

### Get
`<get key>`
~~~
127.0.0.1:6379> get name
"scott"
~~~

### Check Time to Live
Checking when a key will expire is simple.
`<ttl key>`
~~~
127.0.0.1:6379> ttl name
(integer) 28
~~~
This means in 28 seconds this key "should" disappear.

~~~
127.0.0.1:6379> get name
(nil)
~~~
This confirms that the key is no longer known.

## Redis Streams

~~~
127.0.0.1:6379> xadd com:coffeeco:coffee:v1:orders  * timestamp 1625766472513 orderId ord126 storeId st1 customerId ca183 numItems 6 price 48.00
"1625766472513-0"
~~~

### Check that the Stream was Generated
~~~
127.0.0.1:6379> keys *
1) "com:coffeeco:coffee:v1:orders"
~~~

### Finding the Total Stream Items
~~~
127.0.0.1:6379> xlen com:coffeeco:coffee:v1:orders
(integer) 1
~~~

### Fetching the first item in the stream
~~~
127.0.0.1:6379> xrange com:coffeeco:coffee:v1:orders - + COUNT 1
1) 1) "1625766684875-0"
   2)  1) "orderId"
       2) "ord123"
       3) "customerId"
       4) "ca123"
       5) "timestamp"
       6) "1625766472303"
       7) "numItems"
       8) "4"
       9) "price"
      10) "30.00"
~~~

### Deleting Stream Items
~~~

~~~

## Listing all Commands and Command Parameters
~~~
127.0.0.1:6379> COMMAND
~~~
