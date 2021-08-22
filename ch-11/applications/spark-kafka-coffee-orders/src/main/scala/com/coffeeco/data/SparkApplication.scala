package com.coffeeco.data
import com.coffeeco.data.config._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

trait SparkApplication extends App {

  val appName = Configuration.appName

  lazy val sparkConf: SparkConf = {
    val coreConf = new SparkConf()
      .setAppName(appName)
    // merge if missing
    Configuration.Spark.settings.foreach(tuple =>
      coreConf.setIfMissing(tuple._1, tuple._2)
    )
    coreConf
  }

  lazy implicit val sparkSession: SparkSession = {
    SparkSession.builder()
      .config(sparkConf)
      .enableHiveSupport()
      .getOrCreate()
  }

  /**
   * ensure that the application can run correctly, and there is no missing or empty config
   * @param sparkSession The SparkSession
   * @return true if the application is okay to start
   */
  def validateConfig()(implicit sparkSession: SparkSession): Boolean

  def run(): Unit = {
    validateConfig()
  }

}
