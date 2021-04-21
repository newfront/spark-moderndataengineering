package com.coffeeco.data

import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrameReader, SaveMode}

object SparkEventExtractorApp extends SparkApplication {

  val logger: Logger = Logger.getLogger("com.coffeeco.data.SparkEventExtractorApp")

  object Conf {
    val SourceTableName: String = "spark.event.extractor.source.table"
    val DestinationTableName: String = "spark.event.extractor.destination.table"
    val SaveModeName: String = "spark.event.extractor.save.mode"
  }

  // appName
  // sparkConf
  // sparkSession
  lazy val sourceTable: String = sparkSession
    .conf
    .get(Conf.SourceTableName, "")

  lazy val destinationTable: String = sparkSession
    .conf
    .get(Conf.DestinationTableName, "")

  lazy val saveMode: SaveMode = {
    sparkSession.conf.get(Conf.SaveModeName, "ErrorIfExists") match {
      case "Append" => SaveMode.Append
      case "Ignore" => SaveMode.Ignore
      case "Overwrite" => SaveMode.Overwrite
      case _ => SaveMode.ErrorIfExists
    }
  }

  def validate(): Boolean = {
    sourceTable.nonEmpty &&
      destinationTable.nonEmpty &&
      sparkSession.catalog.tableExists(sourceTable)
  }

  def run(saveMode: SaveMode = saveMode): Unit = {
    if (!validate()) throw new RuntimeException("sourceTable or destinationTable are empty or the sourceTable is missing from the spark warehouse")
    SparkEventExtractor(sparkSession)
      .process(sparkSession.table(sourceTable))
      .write
      .mode(saveMode)
      .saveAsTable(destinationTable)
  }

  run()

}
