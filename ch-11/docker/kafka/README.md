## Adding Kafka to your Data Engineering Wheelhouse

I suggest copying the `kafka` directory into your `~/dataengineering/` directory so you have all of these tools at your fingertips. 
~~~
cd ch-11 && cp -r docker/kafka ~/dataengineering/
~~~
Now you can reuse the same kafka runtime for all of you projects.

## Running the Kafka Cluster
~~~
export KAFKA_DOCKER_COMPOSE_PATH=~/dataengineering/kafka/docker-compose.yaml
docker compose \
  -f $KAFKA_DOCKER_COMPOSE_PATH \
  up \
  -d \
  --remove-orphans
~~~

## Creating the Coffee Orders Topic
The following command will create the **CoffeeOrders** topic in Kafka.
~~~
docker exec -it kafka_kafka-0_1 /opt/bitnami/kafka/bin/kafka-topics.sh \
  --create \
  --if-not-exists \
  --topic com.coffeeco.coffee.v1.orders \
  --bootstrap-server kafka_0:9092,kafka_1:9092,kafka_2:9092 \
  --partitions 4 \
  --replication-factor 2
~~~

## Notes and Thanks
The zookeeper and kafka images are provided by [Bitnami](https://github.com/bitnami/bitnami-docker-kafka). Using the `docker-compose.yaml` located in this directory will download these images from [dockerhub](https://hub.docker.com/r/bitnami/kafka/). Bitnami is a verified publisher with over 100M pulls on their project. 

## Troubleshooting
From time to time you will need to troubleshoot your setup. This is natural. Especially if you are switching to new versions of Kafka. The following section provides you with some common commands you may need to use to assist in troubleshooting.

### View all Kafka Processes
The debug process starts off many times getting some information about the container process that are running. Using the following command you can take a look at the running processes.

~~~
docker ps \
  --filter "name=kafka_*" \
  --filter "network=mde" \
  --format "table {{.ID}}\t{{.Image}}\t{{.Names}}\t{{.Mounts}}\t{{.Networks}}" \
  --no-trunc \
  -s
~~~

For more information on listing the docker processes and using filters. Visit the [command line docs](https://docs.docker.com/engine/reference/commandline/ps/). 

### Stopping and Removing all kafka related containers
If you started these processes using docker compose. Eg. 

~~~
export KAFKA_DOCKER_COMPOSE_PATH=~/dataengineering/kafka/docker-compose.yaml
docker compose -f $KAFKA_DOCKER_COMPOSE_PATH up -d --remove-orphans
~~~

then you can use 
~~~
export KAFKA_DOCKER_COMPOSE_PATH=~/dataengineering/kafka/docker-compose.yaml
docker compose down -f $KAFKA_DOCKER_COMPOSE_PATH --remove-orphans
~~~

to turn things back off.

~~~
docker rm kafka_zookeeper_1 kafka_kafka-0_1 kafka_kafka-1_1 kafka_kafka-2_1
~~~

### Remove the Docker Volumes
First off, you can view all kafka related docker volumes using the `docker volume ls` command.

~~~
docker volume ls -f "name=kafka_*"
~~~

Next, for any volume you want to remove, just go ahead and stop and remove the associated container first.For example, `kafka_kafka-0_1` will need to be both stopped and removed `docker stop kafka_kafka-0_1 && docker rm kafka_kafka-0_1` before you can actually delete the `kafka_kafka_0_data` volume. This is in order to preserve the integrity of the data stored in the local volume.

~~~
docker volume rm kafka_kafka_0_data
~~~

## Security
The `docker-compose-acls.yaml` turns on Authorization controls using the broker config `KAFKA_CFG_AUTHORIZER_CLASS_NAME=kafka.security.authorizer.AclAuthorizer`. This will enable you to turn on or off access to individual Topics and to restrict the commands available for connected clients of your cluster. It is worth noting that the configuration `KAFKA_CFG_ALLOW_EVERYONE_IF_NO_ACL_FOUND=true` enables ACLs to be ignored for the Kafka cluster while you are adding rules. You can turn ACL enforcement back on `KAFKA_CFG_ALLOW_EVERYONE_IF_NO_ACL_FOUND=false` after creating `Read` and `Write` controls.

### Add Read and Write Controls
It is in your best interest to govern read and write access from within your Kafka cluster. The following commands show how to add `read`, `write` ACLs for the User's `kafka` and `dataengineering` for the **topic** `com.coffeeco.coffee.v1.orders`.

**Command**
~~~
docker exec -it kafka_kafka-0_1 \
  /opt/bitnami/kafka/bin/kafka-acls.sh \
  --bootstrap-server :9092 \
  --add \
  --allow-principal User:dataengineering \
  --allow-principal User:kafka \
  --operation read \
  --operation write \
  --topic com.coffeeco.coffee.v1.orders
~~~

**Response**
~~~
Adding ACLs for resource `ResourcePattern(resourceType=TOPIC, name=com.coffeeco.coffee.v1.orders, patternType=LITERAL)`: 
 	(principal=User:dataengineering, host=*, operation=WRITE, permissionType=ALLOW)
	(principal=User:dataengineering, host=*, operation=READ, permissionType=ALLOW)
	(principal=User:kafka, host=*, operation=WRITE, permissionType=ALLOW)
	(principal=User:kafka, host=*, operation=READ, permissionType=ALLOW)
~~~

### List Topic ACLs
**List Command**
~~~
docker exec -it kafka_kafka-0_1 \
  /opt/bitnami/kafka/bin/kafka-acls.sh \
  --bootstrap-server :9092 \
  --list \
  --topic com.coffeeco.coffee.v1.orders
~~~

**Response**
~~~
Current ACLs for resource `ResourcePattern(resourceType=TOPIC, name=com.coffeeco.coffee.v1.orders, patternType=LITERAL)`: 
 	(principal=User:kafka, host=*, operation=WRITE, permissionType=ALLOW)
	(principal=User:kafka, host=*, operation=READ, permissionType=ALLOW)
	(principal=User:dataengineering, host=*, operation=WRITE, permissionType=ALLOW)
	(principal=User:dataengineering, host=*, operation=READ, permissionType=ALLOW) 
~~~
