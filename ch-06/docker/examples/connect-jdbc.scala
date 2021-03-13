/*
Usage: (mysql needs to be running)
1. (start up the environment) cd /path/to/ch-05/docker && ./run.sh start
2. (check that MySQL is running and ports are bound) nc -z localhost 3306
3. (run the spark shell with runtime config and local jars) and this scala file
$SPARK_HOME/bin/spark-shell \
  --conf "spark.jdbc.driver.class=com.mysql.cj.jdbc.Driver" \
  --conf "spark.jdbc.host=127.0.0.1" \
  --conf "spark.jdbc.port=3306" \
  --conf "spark.jdbc.default.db=default" \
  --conf "spark.jdbc.table=customers" \
  --conf "spark.jdbc.user=dataeng" \
  --conf "spark.jdbc.password=dataengineering_user" \
  --jars=spark/jars/mariadb-java-client-2.7.2.jar,spark/jars/mysql-connector-java-8.0.23.jar \
  -i examples/connect-jdbc.scala
*/
import org.apache.spark.sql._

// spark: SparkSession (from spark-shell)
assert(spark.isInstanceOf[SparkSession])

// assign from spark conf
val jdbcDriver = spark.conf.get("spark.jdbc.driver.class", "com.mysql.cj.jdbc.Driver")
val dbHost     = spark.conf.get("spark.jdbc.host","localhost")
val dbPort     = spark.conf.get("spark.jdbc.port", "3306")
val defaultDb  = spark.conf.get("spark.jdbc.default.db", "default")
val dbTable    = spark.conf.get("spark.jdbc.table", "customers")
val dbUser     = spark.conf.get("spark.jdbc.user", "dataeng")
val dbPass     = spark.conf.get("spark.jdbc.password")

val connectionUrl = s"jdbc:mysql://$dbHost:$dbPort/$defaultDb"

println(s"mysql.connection.url=$connectionUrl")

// basic: load the remote SQL table
val df = spark
.read
.format("jdbc")
.options(Map[String, String](
    "url" -> connectionUrl,
    "driver" -> jdbcDriver,
    "dbtable" -> dbTable,
    "user" -> dbUser,
    "password" -> dbPass
  )
)
.load()

/*
// advanced: load and partition by timestamp
val df = spark
.read
.format("jdbc")
.options(Map[String, String](
    "url" -> connectionUrl,
    "driver" -> jdbcDriver,
    "dbtable" -> dbTable,
    "user" -> dbUser,
    "password" -> dbPass,
    "numPartitions" -> "2",
    "partitionColumn" -> "created",
    "lowerBound" -> "2021-02-16 02:00:00",
    "upperBound" -> "2021-02-21 21:00:00"
  )
)
.load()
*/

/*
// advanced: load and split by date
val df = spark
.read
.format("jdbc")
.options(Map[String, String](
    "url" -> connectionUrl,
    "driver" -> jdbcDriver,
    "dbtable" -> dbTable,
    "user" -> dbUser,
    "password" -> dbPass,
    "numPartitions" -> "2",
    "partitionColumn" -> "created",
    "lowerBound" -> "2021-02-16",
    "upperBound" -> "2021-02-21"
  )
)
.load()
*/