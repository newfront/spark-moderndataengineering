# Chapter 4 Workshop Material

Setting up your environment is covered in Chapter 2, but as long as you have the following set in your local env `bash or zsh`, you will be golden.
1. JAVA_HOME - (must be java 8 or java 11)
2. SPARK_HOME - (we are using spark 3x)


Start the Docker Container
~~~
cd docker && ./run.sh start
~~~

Go to http://localhost:8080 to see the zeppelin homepage.

### 
If localhost doesn't exist on your setup, you will have to update your `etc/hosts` to include the hostname. Here are the recommended additional host entries for working with the workshop materials.

~~~
##
# Hosts file example for Modern Data Engineering with Apache Spark
#
# add localhost and docker hosts
##
127.0.0.1 localhost
127.0.0.1 zeppelin
127.0.0.1 spark
# End of section
~~~

