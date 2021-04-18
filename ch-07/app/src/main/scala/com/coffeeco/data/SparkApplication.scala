package com.coffeeco.data
import com.coffeeco.data.config._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

trait SparkApplication extends App {

  val appName = Configuration.appName

  lazy val sparkConf: SparkConf = {
    new SparkConf()
      .setAppName(appName)
      .setAll(Configuration.Spark.settings)
  }

  lazy val sparkSession: SparkSession = {
    SparkSession.builder()
      .config(sparkConf)
      .enableHiveSupport()
      .getOrCreate()
  }

}
