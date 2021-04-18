# Chapter 6 Workshop Material

Setting up your environment is covered in Chapter 2, but as long as you have the following set in your local env `bash or zsh`, you will be golden.

1. JAVA_HOME - (must be java 8 or java 11)
2. SPARK_HOME - (we are using spark 3x)


Start the Docker Container
~~~
cd docker && ./run.sh start
~~~

### 
If localhost doesn't exist on your setup, or if you would like to add `zeppelin` as an official host. You will have to update your `etc/hosts` to include the hostname. Here are the recommended additional host entries for working with the workshop materials.

~~~
##
# Hosts file example for Modern Data Engineering with Apache Spark
#
# add localhost and docker hosts
##
127.0.0.1 localhost
127.0.0.1 zeppelin
127.0.0.1 spark
127.0.0.1 mysql
# End of section
~~~

Go to http://localhost:8080, or if you've added the hostname http://zeppelin:8080 to see the zeppelin homepage and continue the exercises.

## MySQL and the Hive Metastore
This chapter introduces you to the Spark SQL Catalog and using the Hive Metastore.

There is a new Docker container that spins up called `mysql`. This is your local MySQL db. It is forwarding port `3306` to your localhost environment. If you already have MySQL running on port `3306` then you can modify the `docker-compose-all.yaml` under `services.mysql.ports` to change the forwarding port.

