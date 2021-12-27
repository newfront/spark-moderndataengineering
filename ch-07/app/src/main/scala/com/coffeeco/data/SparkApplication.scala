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
   * The validation rules is a predicate map called within the scope
   * of the object mixing in the SparkApplication trait at the time validateConfig is called
   */
  lazy val validationRules: Map[()=>Boolean, String] = Map.empty

  /**
   * ensure that the application can run correctly, and there is no missing or empty config
   * @param sparkSession The SparkSession
   * @return true if the application is okay to start
   */
  def validateConfig()(implicit sparkSession: SparkSession): Boolean = {
    if (validationRules.nonEmpty) {
      val results = validationRules.foldLeft[List[String]](List.empty[String])(
        (accumulator: List[String], rule: (()=>Boolean, String)) => {
          // if the predicate is not true, we have a problem
          if (!rule._1()) {
            accumulator :+ rule._2
          } else accumulator
        }
      )

      if (results.nonEmpty) throw new RuntimeException(s"Configuration Issues Encountered:\n ${results.mkString("\n")}")
      true
    } else true
  }

  def run(): Unit = {
    validateConfig()
  }

}
