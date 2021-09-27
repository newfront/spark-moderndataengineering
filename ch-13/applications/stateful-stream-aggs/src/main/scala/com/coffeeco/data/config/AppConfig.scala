package com.coffeeco.data.config

object AppConfig {
  val sourceFormat = "spark.app.source.format"
  val sourceSchemaDDL = "spark.app.source.schemaDDL"
  val sourceStreamOptions = "spark.app.source.options."
  // used to drop late arriving data (if using windowing)
  val sourceWatermarkDuration = "spark.app.source.watermark.duration"
  // windowing support
  val windowTimestampColumn = "spark.app.groupBy.window.timestamp.column"
  val windowDuration = "spark.app.groupBy.window.duration"
  val windowSlideDuration = "spark.app.groupBy.window.slide.duration"
  val windowStartTime = "spark.app.groupBy.window.start.time"

  val sinkFormat = "spark.app.sink.format"
  val sinkQueryName = "spark.app.sink.queryName"
  val sinkStreamOptions = "spark.app.sink.options."

  val sinkTriggerEnabled = "spark.app.sink.trigger.enabled"
  // triggerType defaults to processing, can also be once
  val sinkTriggerType = "spark.app.sink.trigger.type"
  val sinkProcessingInterval = "spark.app.sink.processing.interval"
  // outputMode can only be complete, update, or append
  val sinkOutputMode = "spark.app.sink.outputMode"
  val sinkPartitionBy = "spark.app.sink.partitionBy"
  // partitionBy uses one or more columns (varargs - eg cols*)
  // to handle any issues with commas, you can replace the pattern separator for
  // example with :, so date,hour could be date:hour, etc
  val sinkPartitionBySeparator = ","
  val sinkToTableName = "spark.app.sink.config.tableName"

  val TriggerProcessingTime = "process"
  val TriggerContinuous = "continuous"
  val TriggerOnce = "once"

  val DefaultWindowDuration: String = "15 minutes"
}
