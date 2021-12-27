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

  /**
   * The validation rules test the expected behavior (left hand predicate)
   * if the predicate returns True, then we are good, otherwise, we have an exception
   * to the validation rules
   */
  override lazy val validationRules = Map[() => Boolean, String](
    (()=> sourceTable.nonEmpty) -> s"${Conf.SourceTableName} can not be an empty value",
    (()=> destinationTable.nonEmpty) -> s"${Conf.DestinationTableName} can not be an empty value",
    (()=> sourceTable != destinationTable) -> s"The source table ${Conf.SourceTableName}:$sourceTable can not be the same as your destination table ${Conf.DestinationTableName}:$destinationTable.",
    (()=> sparkSession.catalog.tableExists(sourceTable)) -> s"The source table ${Conf.SourceTableName}:$sourceTable} does not exist"
  )

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
