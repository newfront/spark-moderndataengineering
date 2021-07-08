// cd ~/ch-09/data/avro && $SPARK_HOME/bin/spark-shell --packages org.apache.spark:spark-avro_2.12:3.1.1
// :load read_avro.scala
// see http://spark.apache.org/docs/latest/sql-data-sources-avro.html for more examples

import java.nio.file._
import org.apache.spark.sql.avro.functions._
import org.apache.spark.sql._
import org.apache.spark.sql.types._

case class Coffee(id: String, name: String, boldness: Int, available: Boolean)

val jsonFormatSchema = new String(Files.readAllBytes(Paths.get("./coffee.avsc")))

val coffee = spark.createDataFrame[Coffee](
  Seq(Coffee("co123","verve",3,true),Coffee("co231","folgers",9,true))
)

/* write the simple dataframe as avro */
coffee
  .repartition(1)
  .write
  .format("avro")
  .mode("overwrite")
  .save("coffee_list.avro")

