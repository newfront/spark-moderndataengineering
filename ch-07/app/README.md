## CoffeeCo: Chapter 7 : Structured Reference Application

### Installing SBT
SBT is a scala build tool. You can download it from [scala-sbt.org](https://www.scala-sbt.org/)

Installing `sbt` can be done easily with Homebrew if you have it installed: `brew install sbt`

### Building the Application
The application can be built using SBT, Java 11 and Scala 2.12.
You should have installed Scala 2.12 and Java 11 per the exercise from Chapter 2. 

~~~
sbt clean assembly
~~~

### Reuse what you've learned
This application is an example batch application that reuses the Hive Metastore you created in Chapter 6.
You just need to either: 
  1. run mysql on docker (using ch-06/docker/), or 
  2. run mysql locally and just make sure you bootstrap the environment
  3. Update the `conf/local.conf` to include the correct jdbc connection url for you MySQL host (currently it is using the docker hostname)

### Adding the Bronze and Silver databases (hive/spark.sql.warehouse)
Working with distributed data can be difficult. For data in transit, it can be easier to do the following:
1. use the notion of a `bronze` database for raw data
2. use the notion of a `silver` database for reliable data
3. use the notion of a `gold` database for production data

### Running the Application (Exercise 7-3)
In order to run the application, you'll need to do two things:
1. You'll need to build the application. `sbt clean assembly`
2. Populate the `default.customers` table (see src/test/resources/customers/customers.json) in the `metastore`
3. Populate the `bronze.customerRatings` table (see src/test/resources/customers/customer_ratings.json) in the `metastore`
4. Modify the `spark-submit` in order to run your application. You'll need to update the `spark.sql.warehouse.dir`
~~~
$SPARK_HOME/bin/spark-submit \
  --master "local[*]" \
  --class "com.coffeeco.data.SparkEventExtractorApp" \
  --deploy-mode "client" \
  --packages=org.mariadb.jdbc:mariadb-java-client:2.7.2 \
  --conf "spark.event.extractor.source.table=bronze.customerRatings" \
  --conf "spark.event.extractor.destination.table=silver.customerRatings" \
  --conf "spark.sql.hive.javax.jdo.option.ConnectionDriverName=org.mariadb.jdbc.Driver" \
  --conf "spark.event.extractor.save.mode=ErrorIfExists" \
  --conf "spark.sql.hive.javax.jdo.option.ConnectionPassword=dataengineering_user" \
  --conf "spark.sql.warehouse.dir=file:///PATH/TO/spark-moderndataengineering/ch-06/docker/spark/sql/warehouse" \
  --driver-java-options "-Dconfig.file=conf/local.conf" \
  target/scala-2.12/spark-event-extractor-assembly-0.1-SNAPSHOT.jar
~~~

5. Now you should have your first end-to-end pipeline job completed.
