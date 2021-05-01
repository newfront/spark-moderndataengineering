import sbt.Keys._
import sbt.Project.projectToLocalProject


lazy val root = (project in file(".")).settings(
  version := "0.1-SNAPSHOT",
  organization := "com.coffeeco.data",
  name := "spark-event-extractor",
  scalaVersion := "2.12.10",
  mainClass in Compile := Some("com.coffeeco.data.SparkEventExtractorApp")
)

val sparkVersion = "3.1.1"

resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark"%%"spark-hive" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
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

