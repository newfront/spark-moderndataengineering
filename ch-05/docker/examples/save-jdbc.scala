/*
 
  You can load this file into the spark-shell at runtime using the following
  1. cd /path/to/ch-05/docker
 
  2. Start the Spark Shell (preloads connect-jdbc.scala) using the following
  $SPARK_HOME/bin/spark-shell \
    --conf "spark.jdbc.driver.class=com.mysql.cj.jdbc.Driver" \
    --conf "spark.jdbc.host=127.0.0.1" \
    --conf "spark.jdbc.port=3306" \
    --conf "spark.jdbc.default.db=default" \
    --conf "spark.jdbc.table=customers" \
    --conf "spark.jdbc.user=dataeng" \
    --conf "spark.jdbc.password=dataengineering_user" \
    --packages=mysql:mysql-connector-java:8.0.23 \
    --jars=spark/jars/mariadb-java-client-2.7.2.jar \
    -i examples/connect-jdbc.scala
  3. :load examples/save-jdbc.scala 
*/

import org.apache.spark.sql._
import java.sql.Timestamp

// spark: SparkSession (from spark-shell)
assert(spark.isInstanceOf[SparkSession])

def ts(timeStr: String): Timestamp = Timestamp.valueOf(timeStr)

// create some new customers
val records = Seq(
  Row("4",ts("2021-02-21 21:00:00"),ts("2021-02-21 21:00:00"),"Penny","Haines","penny@coffeeco.com"),
  Row("5",ts("2021-02-21 22:00:00"),ts("2021-02-21 22:00:00"),"Cloud","Fast","cloud.fast@acme.com"),
  Row("6",ts("2021-02-21 23:00:00"),ts("2021-02-21 23:00:00"),"Marshal","Haines","paws@coffeeco.com")
)

// generate a new DataFrame using the new records and the schema from df (connect-jdbc.scala)
val customers = spark.createDataFrame(
    spark.sparkContext.parallelize(records),
    df.schema
)

// note: (doesn't deduplicate)
customers
  .write
  .format("jdbc")
  .mode("append")
  .options(Map[String, String](
    "url" -> connectionUrl,
    "driver" -> jdbcDriver,
    "dbtable" -> dbTable,
    "user" -> dbUser,
    "password" -> dbPass
  )
)
.save()

/*
// write into the database, fails if the table already exists
customers
  .write
  .format("jdbc")
  .mode("errorIfExists")
  .options(Map[String, String](
    "url" -> connectionUrl,
    "driver" -> jdbcDriver,
    "dbtable" -> dbTable,
    "user" -> dbUser,
    "password" -> dbPass
  )
)
.save()
*/