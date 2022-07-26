package com.coffeeco.data

import org.apache.log4j.Logger
import org.apache.spark.sql.{SaveMode, SparkSession}

object SparkEventExtractorApp extends SparkBatchApplication {

  val logger: Logger = Logger.getLogger("com.coffeeco.data.SparkEventExtractorApp")

  object Conf {
    final val SourceTableName: String = "spark.event.extractor.source.table"
    final val DestinationTableName: String = "spark.event.extractor.destination.table"
    final val SaveModeName: String = "spark.event.extractor.save.mode"
  }

  lazy val sourceTable: String = sparkSession
    .conf
    .get(Conf.SourceTableName, "")

  lazy val destinationTable: String = sparkSession
    .conf
    .get(Conf.DestinationTableName, "")

  override lazy val saveMode: SaveMode = {
    sparkSession.conf.get(Conf.SaveModeName, "ErrorIfExists") match {
      case "Append" => SaveMode.Append
      case "Ignore" => SaveMode.Ignore
      case "Overwrite" => SaveMode.Overwrite
      case _ => SaveMode.ErrorIfExists
    }
  }

  override def validateConfig()
    (implicit sparkSession: SparkSession): Boolean = {
    val isValid = sourceTable.nonEmpty &&
      destinationTable.nonEmpty &&
      sourceTable != destinationTable &&
      sparkSession.catalog.tableExists(sourceTable)
    if (!isValid) throw new RuntimeException(
      s"${Conf.SourceTableName} or ${Conf.DestinationTableName} are empty, " +
        s"or the $sourceTable is missing from the spark warehouse")
    true
  }

  override def runBatch(saveMode: SaveMode): Unit = {
    //
    SparkEventExtractor(sparkSession)
      .transform(sparkSession.table(sourceTable))
      .write
      .mode(saveMode)
      .saveAsTable(destinationTable)
  }

  // always use run() to trigger the application.
  // this enables the config validation to run before triggering the batch
  run()

}
