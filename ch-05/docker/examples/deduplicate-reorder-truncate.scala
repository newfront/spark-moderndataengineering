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
  4. :load examples/deduplicate-reorder-truncate.scala
*/

// note if you accidently delete everything
/*
import java.sql.Timestamp
def ts(timeStr: String): Timestamp = Timestamp.valueOf(timeStr)

// create some new customers
val records = Seq(
  Row("1", ts("2021-02-16 00:16:06"),ts("2021-02-16 00:16:06"), "Scott", "Haines", "scott@coffeeco.com"),
  Row("2", ts("2021-02-16 00:16:06"),ts("2021-02-16 00:16:06"), "John", "Hamm", "john.hamm@acme.com"),
  Row("3", ts("2021-02-16 00:16:06"),ts("2021-02-16 00:16:06"), "Milo", "Haines", "mhaines@coffeeco.com"),
  Row("4",ts("2021-02-21 21:00:00"),ts("2021-02-21 21:00:00"),"Penny","Haines","penny@coffeeco.com"),
  Row("5",ts("2021-02-21 22:00:00"),ts("2021-02-21 22:00:00"),"Cloud","Fast","cloud.fast@acme.com"),
  Row("6",ts("2021-02-21 23:00:00"),ts("2021-02-21 23:00:00"),"Marshal","Haines","paws@coffeeco.com")
)

val customers = spark.createDataFrame(
    spark.sparkContext.parallelize(records),
    df.schema
)

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
*/

import org.apache.spark.sql.SaveMode

// store the deduplicated data as a table
// given the small number of rows (6 in the example)
// we can coalesce the underlying DataFrame RDDs down to 1 partition
// this means we can keep the insert order
// with more than one partition we can't maintain the insert order
// since it will be inserting across threads or executors

df
  .dropDuplicates("id")
  .sort(asc("id"))
  .coalesce(1)
  .write
  .mode("overwrite")
  .saveAsTable("temp_customers")

// truncate the original table (clear rows) - this is destructive so be warned
df
  .write
  .format("jdbc")
  .mode(SaveMode.Overwrite)
  .options(Map[String, String](
    "url" -> connectionUrl,
    "driver" -> jdbcDriver,
    "dbtable" -> dbTable,
    "user" -> dbUser,
    "password" -> dbPass,
    "truncate" -> "true"
  )
)
.save()

// overwrite the table using the persisted table
spark
  .table("temp_customers")
  .write.format("jdbc")
  .mode(SaveMode.Overwrite)
  .options(Map[String, String](
    "url" -> connectionUrl,
    "driver" -> jdbcDriver,
    "dbtable" -> dbTable,
    "user" -> dbUser,
    "password" -> dbPass
  )
)
.save()

// drop the persisted table
spark.sql("drop table if exists temp_customers").show
