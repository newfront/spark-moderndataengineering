lazy val root = (project in file(".")).settings(
  version := "0.1-SNAPSHOT",
  organization := "com.coffeeco.data",
  name := "spark-redis-streams",
  scalaVersion := "2.12.10",
  mainClass in Compile := Some("com.coffeeco.data.SparkRedisStreamsApp")
)

val sparkVersion = "3.1.2"
val commonsPoolVersion = "2.0"
val jedisVersion = "3.2.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark"%%"spark-hive" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.commons" % "commons-pool2" % commonsPoolVersion,
  "redis.clients" % "jedis" % jedisVersion,
  "com.typesafe" % "config" % "1.3.1",
  "org.scalactic" %% "scalactic" % "3.2.0",
  "org.apache.spark" %% "spark-sql" % sparkVersion % Test classifier "tests",
  "org.apache.spark" %% "spark-sql" % sparkVersion % Test classifier "test-sources",
  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.4.2" % Test,
  "com.holdenkarau" %% "spark-testing-base" % "3.0.1_1.0.0" % Test
)

parallelExecution in Test := false
fork in Test := true
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")

