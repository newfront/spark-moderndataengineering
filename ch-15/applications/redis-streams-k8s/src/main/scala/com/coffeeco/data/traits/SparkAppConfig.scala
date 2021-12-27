package com.coffeeco.data.traits

trait SparkAppConfig {
  val sourceFormat: String = "spark.app.source.format"
  val sourceFormatDefault: String = "kafka"
  val sourceSchemaDDL: String = "spark.app.source.schemaDDL"
  val sourceStreamOptions: String = "spark.app.source.options."
  // used to drop late arriving data (if using windowing)
  val sourceWatermarkDuration: String = "spark.app.source.watermark.duration"
  // windowing support
  val windowTimestampColumn: String = "spark.app.groupBy.window.timestamp.column"
  val windowTimestampColumnDefault: String = "timestamp"
  val windowDuration: String = "spark.app.groupBy.window.duration"
  val windowSlideDuration: String = "spark.app.groupBy.window.slide.duration"
  val windowStartTime: String = "spark.app.groupBy.window.start.time"

  val sinkFormat: String = "spark.app.sink.format"
  val sinkFormatDefault: String = "parquet"
  val sinkQueryName: String = "spark.app.sink.queryName"
  val SinkQueryNameDefault: String = "output_stream"
  val sinkStreamOptions: String = "spark.app.sink.options."

  val sinkTriggerEnabled: String = "spark.app.sink.trigger.enabled"
  // triggerType defaults to processing, can also be once
  val sinkTriggerType: String = "spark.app.sink.trigger.type"
  val sinkProcessingInterval: String = "spark.app.sink.processing.interval"
  // outputMode can only be complete, update, or append
  val sinkOutputMode: String = "spark.app.sink.outputMode"
  val sinkPartitionBy: String = "spark.app.sink.partitionBy"
  // partitionBy uses one or more columns (varargs - eg cols*)
  // to handle any issues with commas, you can replace the pattern separator for
  // example with :, so date,hour could be date:hour, etc
  val sinkPartitionBySeparator: String = ","
  val OutputModeOption: String = "outputMode"
  val PartitionByOption: String = "partitionBy"
  val TriggerOption: String = "trigger"
  val DataSourceOptions: String = "options"
  val sinkToTableName = "spark.app.sink.output.tableName"

  val TriggerProcessingTime: String = "process"
  val TriggerContinuous: String = "continuous"
  val TriggerOnce: String = "once"
  val SinkProcessingIntervalDefault: String = "1 minute"
  val DefaultWindowDuration: String = "15 minutes"
  val DefaultWatermarkDuration: String = "15 minutes"

}
