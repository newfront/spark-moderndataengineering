# Chapter 12: Towards Advanced Analytical Processing & Insights
This chapter marks the end of a journey across the many supporting pillars encompassing the art of modern data engineering. In order to run these final exercises, you will need to be running a custom Apache Zeppelin container. This will reduce the amount of work necessary to interoperate with the rest of the materials from the book.

## Building Zeppelin with Custom Spark Support

1. Build the container.
~~~
cd ch-12/docker/zeppelin-spark &&
docker build . -t `whoami`/zeppelin-spark:latest
~~~

You can also use the container from [`newfrontdocker/zeppelin-spark:0.9.0-spark3.1.2`](https://hub.docker.com/layers/166864655/newfrontdocker/zeppelin-spark/0.9.0-spark3.1.2/images/sha256-af692753122fdedee9882691516fac79d665107a63a075326ba65d8855dbaedc?context=repo).

2. Copy into your `~/dataengineering` root directory
~~~
cd ch-12/docker &&
cp -r zeppelin ~/dataengineering/zeppelin-spark
~~~

3. Run the Environment
~~~
docker compose -f ~/dataengineering/zeppelin-spark/docker-compose.yaml up -d
~~~

4. Stop the Environment
~~~
docker compose -f ~/dataengineering/zeppelin-spark/docker-compose.yaml down --remove-orphans
~~~
