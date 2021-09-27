package com.coffeeco.data.traits

import com.coffeeco.data.config.AppConfig
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{DataStreamReader, DataStreamWriter, StreamingQuery, Trigger}

/**
 * Creates a generic mix-in for StructuredStreaming applications
 * @tparam T The type of either DataFrame or Dataset[_]
 * @tparam U The type parameter for the DataStreamWriter
 */
trait SparkStructuredStreamingApplication[T, U] extends SparkApplication {

  lazy val triggerType: String = sparkConf.get(AppConfig.sinkTriggerType, defaultValue = "ProcessingTime")

  override def validateConfig()(implicit sparkSession: SparkSession): Boolean = {
    // ensure we have at least source/sink options
    if (
      sparkConf.get(AppConfig.sourceFormat,"").nonEmpty &&
      sparkConf.get(AppConfig.sinkFormat,"").nonEmpty &&
      sparkConf.getAllWithPrefix(AppConfig.sourceStreamOptions).nonEmpty &&
      sparkConf.getAllWithPrefix(AppConfig.sinkStreamOptions).nonEmpty) {
      true
    } else throw new RuntimeException(s"You are missing either your source or stream config options. " +
      s"Check that ${AppConfig.sourceFormat}, ${AppConfig.sinkFormat}, ${AppConfig.sourceStreamOptions}, and ${AppConfig.sinkStreamOptions} are configured")
  }

  // Generic DataSource (DataStreamReader)
  def streamReader: DataStreamReader = {

    // build initial DataStreamReader
    sparkSession.readStream
      .format(sparkConf.get(AppConfig.sourceFormat, "redis"))
      .options(sparkConf.getAllWithPrefix(
        sparkConf.get(AppConfig.sourceStreamOptions)
      ).toMap)
  }

  lazy val inputStream: DataStreamReader = streamReader

  def outputStream(writer: DataStreamWriter[U])(implicit sparkSession: SparkSession): DataStreamWriter[U] = {
    val sinkFormat = sparkSession.conf.get(AppConfig.sinkFormat, "parquet")

    Seq("outputMode", "partitionBy", "trigger").foldLeft[DataStreamWriter[U]](writer)((w, config) => {
      config match {
        case "outputMode" =>
          writer.outputMode(sparkSession.conf.get(AppConfig.sinkOutputMode, "append").trim)
        case "partitionBy" if sparkSession.conf.get(AppConfig.sinkPartitionBy, "").nonEmpty =>
          // used for partitioned output (parquet data sink, fs sink, etc)
          val splitPattern = sparkSession.conf.get(AppConfig.sinkPartitionBySeparator, ",")
          val partitionColumns = sparkSession.conf.get(AppConfig.sinkPartitionBy).split(splitPattern).map(_.trim)
          if (partitionColumns.nonEmpty) writer.partitionBy(partitionColumns:_*) else writer
        case "trigger" if sparkConf.getBoolean(AppConfig.sinkTriggerEnabled,defaultValue = false) =>
          sparkConf.get(AppConfig.sinkTriggerType, defaultValue = AppConfig.TriggerProcessingTime).toLowerCase match {
            case AppConfig.TriggerProcessingTime =>
              val processTimeInterval = sparkSession.conf.get(AppConfig.sinkProcessingInterval, "1 minute")
              writer.trigger(Trigger.ProcessingTime(processTimeInterval))
            case AppConfig.TriggerOnce =>
              writer.trigger(Trigger.Once())
            case AppConfig.TriggerContinuous if sinkFormat.equals("kafka") =>
              // only kafka supports this type
              // reusing the processing interval as checkpoint interval
              val checkpointInterval = sparkSession.conf.get(AppConfig.sinkProcessingInterval, "1 minute")
              writer.trigger(Trigger.Continuous(checkpointInterval))
            case _ =>
              writer
          }
        case _ =>
          writer
      }
    })
      .format(sinkFormat)
      .queryName(sparkSession.conf.get(AppConfig.sinkQueryName))
  }

  def startAndAwaitApp(query: StreamingQuery): Unit = {
    query.awaitTermination()
  }

}
