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
  --conf "spark.sql.query=select id, first_name, email from customers where email LIKE '%coffeeco%' ORDER BY email ASC LIMIT 3" \
  --conf "spark.jdbc.user=dataeng" \
  --conf "spark.jdbc.password=dataengineering_user" \
  --jars=spark/jars/mariadb-java-client-2.7.2.jar,spark/jars/mysql-connector-java-8.0.23.jar \
  -i examples/jdbc-adhoc-query.scala
*/
import org.apache.spark.sql._

// spark: SparkSession (from spark-shell)
assert(spark.isInstanceOf[SparkSession])

// assign from spark conf
val jdbcDriver = spark.conf.get("spark.jdbc.driver.class", "com.mysql.cj.jdbc.Driver")
val dbHost     = spark.conf.get("spark.jdbc.host","localhost")
val dbPort     = spark.conf.get("spark.jdbc.port", "3306")
val defaultDb  = spark.conf.get("spark.jdbc.default.db", "default")
val dbUser     = spark.conf.get("spark.jdbc.user", "dataeng")
val dbPass     = spark.conf.get("spark.jdbc.password")

val query      = spark.conf.get("spark.sql.query", s"select * from customers limit 5")

val connectionUrl = s"jdbc:mysql://$dbHost:$dbPort/$defaultDb"

println(s"mysql.connection.url=$connectionUrl")

// basic: load the remote SQL table
val df = spark
.read
.format("jdbc")
.options(Map[String, String](
    "url" -> connectionUrl,
    "driver" -> jdbcDriver,
    "query" -> query,
    "user" -> dbUser,
    "password" -> dbPass
  )
)
.load()

println(s"running query: $query")

df.show(truncate=false)

println("query executed")
// exit the spark-shell
sys.exit(0)
