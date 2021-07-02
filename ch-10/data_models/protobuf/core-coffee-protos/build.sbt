import sbt.Keys._
import sbt.Project.projectToLocalProject


lazy val root = (project in file(".")).settings(
  version := "0.1-SNAPSHOT",
  organization := "com.coffeeco.protocol",
  name := "core-proto",
  scalaVersion := "2.12.10"/*,
  mainClass in Compile := Some("com.coffeeco.data.SparkEventExtractorApp")*/
)

val sparkVersion = "3.1.1"

resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "com.thesamet.scalapb" %% "scalapb-json4s" % scalapb.compiler.Version.scalapbVersion % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.4.2" % Test,
  "com.holdenkarau" %% "spark-testing-base" % "3.0.1_1.0.0" % Test
)

Compile / PB.targets := Seq(
  PB.gens.java -> (Compile / sourceManaged).value,
  scalapb.gen(javaConversions = true) -> (Compile / sourceManaged).value
)

parallelExecution in Test := false
fork in Test := true
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")

