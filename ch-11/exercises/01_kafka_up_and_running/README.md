# Exercise 1: Getting up and running on Apache Kafka
You will have to have your Kafka cluster running. See `/ch-11/docker/kafka/README.md` for the details.

## Create a Kafka Topic
Creating a topic can be done using the `kafka-topics.sh` or via an official Kafka client. You can run the following command to create a topic on the cluster you spun up for this exercise.
~~~
docker exec \
  -it kafka_kafka-0_1 \
  /opt/bitnami/kafka/bin/kafka-topics.sh \
  --create \
  --if-not-exists \
  --topic com.coffeeco.coffee.v1.orders \
  --bootstrap-server kafka_0:9092,kafka_1:9092,kafka_2:9092 \
  --partitions 4 \
  --replication-factor 2
~~~

## List Topics
This command will show you all topics available in your kafka cluster.
~~~
docker exec \
  -it kafka_kafka-0_1 \
  /opt/bitnami/kafka/bin/kafka-topics.sh \
  --list \
  --bootstrap-server kafka_0:9092,kafka_1:9092,kafka_2:9092
~~~

## Topic Configuration
Topics can be [configured with a robust number of options](https://kafka.apache.org/documentation/#topicconfigs) enabling full control of the life cycle of a topic, the expiration of data within the topic, and how individual events behave (including how duplicate keys can be squashed), as well as how to safe-gaurd late arriving data.

## Describe a Kafka Topic
This will show you the topic level metadata associated with the Kafka topic.
~~~
docker exec \
  -it kafka_kafka-0_1 \
  /opt/bitnami/kafka/bin/kafka-topics.sh \
  --describe \
  --topic com.coffeeco.coffee.v1.orders \
  --bootstrap-server kafka_0:9092,kafka_1:9092,kafka_2:9092
~~~

** Example Output of the Topic Description**
~~~
Topic: com.coffeeco.coffee.v1.orders	TopicId: eENgTy01QUWOAe2gJzi_rw	PartitionCount: 4	ReplicationFactor: 2	Configs: segment.bytes=1073741824
	Topic: com.coffeeco.coffee.v1.orders	Partition: 0	Leader: 2	Replicas: 2,1	Isr: 1,2
	Topic: com.coffeeco.coffee.v1.orders	Partition: 1	Leader: 1	Replicas: 1,0	Isr: 1,0
	Topic: com.coffeeco.coffee.v1.orders	Partition: 2	Leader: 0	Replicas: 0,2	Isr: 0,2
	Topic: com.coffeeco.coffee.v1.orders	Partition: 3	Leader: 2	Replicas: 2,0	Isr: 0,2
~~~

## Configure the Number of Insync Replicas for a Topic
~~~
docker exec \
  -it kafka_kafka-0_1 \
  /opt/bitnami/kafka/bin/kafka-configs.sh \
  --alter \
  --bootstrap-server kafka_0:9092,kafka_1:9092,kafka_2:9092 \
  --entity-type topics \
  --entity-name com.coffeeco.coffee.v1.orders \
  --add-config min.insync.replicas=2
~~~

**Response**
~~~
Completed updating config for topic com.coffeeco.coffee.v1.orders.
~~~

## Increase the Partition Replicas for a Topic
In the case where you want to increase overall topic durability, for example to ensure you have at least 2 copies of each partition in the case of a zone or rack failure, you can use the information from the `describe` command to build a replication assignments file. This can then be used to modify the replication level of the topic. (default from the create command is a replication-factor of 2)

**Note**: The `replication-reassignments.json` is located on the `ch-11/exercises/01_kafka_up_and_running` directory.
~~~
docker cp replication-reassignments.json \
  kafka_kafka-0_1:/opt/replication-reassignments.json
~~~

**Execute the Increase in Replication**
~~~
docker exec \
  -it kafka_kafka-0_1 \
  /opt/bitnami/kafka/bin/kafka-reassign-partitions.sh \
  --bootstrap-server kafka_0:9092,kafka_1:9092,kafka_2:9092 \
  --reassignment-json-file /opt/replication-reassignments.json \
  --execute
~~~

**Response**
~~~
Current partition replica assignment

{"version":1,"partitions":[{"topic":"com.coffeeco.coffee.v1.orders","partition":0,"replicas":[2,1],"log_dirs":["any","any"]},{"topic":"com.coffeeco.coffee.v1.orders","partition":1,"replicas":[1,0],"log_dirs":["any","any"]},{"topic":"com.coffeeco.coffee.v1.orders","partition":2,"replicas":[0,2],"log_dirs":["any","any"]},{"topic":"com.coffeeco.coffee.v1.orders","partition":3,"replicas":[2,0],"log_dirs":["any","any"]}]}

Save this to use as the --reassignment-json-file option during rollback
Successfully started partition reassignments for com.coffeeco.coffee.v1.orders-0,com.coffeeco.coffee.v1.orders-1,com.coffeeco.coffee.v1.orders-2,com.coffeeco.coffee.v1.orders-3
~~~

**Verify the Replication Completed as Expected**
~~~
docker exec \
  -it kafka_kafka-0_1 \
  /opt/bitnami/kafka/bin/kafka-reassign-partitions.sh \
  --bootstrap-server kafka_0:9092,kafka_1:9092,kafka_2:9092 \
  --reassignment-json-file /opt/replication-reassignments.json \
  --verify
~~~

**Success Response**
~~~
Status of partition reassignment:
Reassignment of partition com.coffeeco.coffee.v1.orders-0 is complete.
Reassignment of partition com.coffeeco.coffee.v1.orders-1 is complete.
Reassignment of partition com.coffeeco.coffee.v1.orders-2 is complete.
Reassignment of partition com.coffeeco.coffee.v1.orders-3 is complete.

Clearing broker-level throttles on brokers 0,1,2
Clearing topic-level throttles on topic com.coffeeco.coffee.v1.orders
~~~

## Delete a Topic
**Warning: Deleting a topic can be catastrophic. Do so only when the topic is end-of-life or when there are zero dependent systems relying on the source of data of a given topic**

In order to `really` delete a topic, you will have to modify the brokers in the `docker-compose.yaml`. Enabling topic deletion for broker 0 is shown below, follow the same pattern for all of your brokers.

~~~
kafka-0:
    image: docker.io/bitnami/kafka:2.8.0
    volumes:
      - kafka_0_data:/bitnami/kafka
    expose:
      - 9092
    environment:
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper-server:2181
      - KAFKA_CFG_DELETE_TOPIC_ENABLE=true
      - KAFKA_ZOOKEEPER_USER=kafka
      - KAFKA_ZOOKEEPER_PASSWORD=kafka_password
      - KAFKA_CFG_BROKER_ID=0
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CLIENT:PLAINTEXT,EXTERNAL:PLAINTEXT
      - KAFKA_CFG_LISTENERS=CLIENT://:9092,EXTERNAL://:9093
      - KAFKA_CFG_ADVERTISED_LISTENERS=CLIENT://kafka_0:9092,EXTERNAL://localhost:9093
      - KAFKA_INTER_BROKER_LISTENER_NAME=CLIENT
    ports:
      - "9093:9093"
    networks:
      - mde
    hostname: kafka_0
    restart: always
    depends_on:
      - zookeeper
~~~

Add the `KAFKA_CFG_DELETE_TOPIC_ENABLE=true` flag to all the kafka brokers and then restart the cluster.

**Delete a Topic**
~~~
docker exec \
  -it kafka_kafka-0_1 \
  /opt/bitnami/kafka/bin/kafka-topics.sh \
  --delete \
  --topic com.coffeeco.coffee.v1.orders \
  --bootstrap-server kafka_0:9092,kafka_1:9092,kafka_2:9092
~~~

The `KAFKA_CFG_*` pattern used in the environment variables configure the brokers `server.properties` file. If you don't add the `KAFKA_CFG_DELETE_TOPIC_ENABLE=true` then the topic deletion will be a soft delete. Meaning that the actual topic data will still exist on the kafka brokers, and only the zookeeper record will be removed.

You will see the following exception when trying to `describe` a topic after a soft delete.

~~~
Error while executing topic command : Topic 'com.coffeeco.coffee.v1.orders' does not exist as expected
[2021-08-22 01:16:38,140] ERROR java.lang.IllegalArgumentException: Topic 'com.coffeeco.coffee.v1.orders' does not exist as expected
	at kafka.admin.TopicCommand$.kafka$admin$TopicCommand$$ensureTopicExists(TopicCommand.scala:542)
	at kafka.admin.TopicCommand$AdminClientTopicService.describeTopic(TopicCommand.scala:317)
	at kafka.admin.TopicCommand$.main(TopicCommand.scala:69)
	at kafka.admin.TopicCommand.main(TopicCommand.scala)
~~~


