lazy val root = (project in file(".")).settings(
  version := "0.1-SNAPSHOT",
  organization := "com.coffeeco.data",
  name := "spark-kafka-coffee-orders",
  scalaVersion := "2.12.10",
  Compile / mainClass := Some("com.coffeeco.data.SparkKafkaCoffeeOrdersApp")
)

val sparkVersion = "3.1.2"
val scalaPbVersion = "0.11.0"
val scalaPbJson4SVersion = "0.11.1"
val jacksonCoreVersion = "2.10.0"
val commonsPoolVersion = "2.8.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-hive" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "org.apache.commons" % "commons-pool2" % commonsPoolVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion % Compile,
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonCoreVersion % Provided,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonCoreVersion % Provided,
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonCoreVersion % Provided,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.2",
  "com.thesamet.scalapb" %% "sparksql-scalapb" % scalaPbVersion,
  "com.typesafe" % "config" % "1.3.1",
  "org.scalactic" %% "scalactic" % "3.2.0",
  "org.apache.spark" %% "spark-sql" % sparkVersion % Test classifier "tests",
  "org.apache.spark" %% "spark-sql" % sparkVersion % Test classifier "test-sources",
  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
  "com.thesamet.scalapb" %% "scalapb-json4s" % scalaPbJson4SVersion % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.4.2" % Test,
  "com.holdenkarau" %% "spark-testing-base" % "3.0.1_1.0.0" % Test
)

Test / parallelExecution := false
Test / fork := true
Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")

// See for more details : https://github.com/sbt/sbt-assembly
assembly / assemblyMergeStrategy := {
  case "module-info.class" => MergeStrategy.discard
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps@_*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs@_*) =>
    xs map {
      _.toLowerCase
    } match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps@(x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: "org.apache.spark.sql.sources.DataSourceRegister" :: Nil =>
        // important: This enables the external kafka data source to be used without the fully qualified class name.
        // Essentially, you get to use df.write.format("kafka") vs df.write.format("org.apache.spark.sql.kafka010.KafkaSourceProvider")
        // you decide what is nicer
        MergeStrategy.concat
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.deduplicate
    }
  case _ =>
    MergeStrategy.deduplicate
}

// Hadoop contains an old protobuf runtime that is not binary compatible with Protobuf
// with 3.0.0.  We shaded ours to prevent runtime issues.
assembly / assemblyShadeRules := Seq(
  ShadeRule.rename("com.google.protobuf.**" -> "shadeproto.@1").inAll,
  ShadeRule.rename("scala.collection.compat.**" -> "scalacompat.@1").inAll
)
