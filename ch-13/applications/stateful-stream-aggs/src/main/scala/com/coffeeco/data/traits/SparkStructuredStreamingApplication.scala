package com.coffeeco.data.traits

import com.coffeeco.data.config.AppConfig
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{DataStreamReader, DataStreamWriter, StreamingQuery, Trigger}

/**
 * Creates a generic mix-in for Spark Structured Streaming Applications
 *
 * @tparam T The type of either DataFrame or Dataset[_]
 * @tparam U The type parameter for the DataStreamWriter
 */
trait SparkStructuredStreamingApplication[T, U] extends SparkApplication {
  val appConfig: SparkAppConfig = AppConfig

  override def validateConfig()(implicit sparkSession: SparkSession): Boolean = {
    // ensure we have at least source/sink options
    if (
      sparkConf.get(appConfig.sourceFormat, "").nonEmpty &&
        sparkConf.get(appConfig.sinkFormat, "").nonEmpty &&
        sparkConf.getAllWithPrefix(appConfig.sourceStreamOptions).nonEmpty &&
        sparkConf.getAllWithPrefix(appConfig.sinkStreamOptions).nonEmpty) {
      true
    } else throw new RuntimeException(s"You are missing either your source or stream config options. " +
      s"Check that ${appConfig.sourceFormat}, ${appConfig.sinkFormat}, ${appConfig.sourceStreamOptions}, and " +
      s"${appConfig.sinkStreamOptions} are configured")
  }

  // Generic DataSource (DataStreamReader)
  def streamReader()(implicit spark: SparkSession): DataStreamReader = {
    spark.readStream
      .format(sparkConf.get(appConfig.sourceFormat, appConfig.sourceFormatDefault))
      .options(sparkConf.getAllWithPrefix(
        sparkConf.get(appConfig.sourceStreamOptions)
      ).toMap)
  }

  lazy val inputStream: DataStreamReader = streamReader

  def outputStream(writer: DataStreamWriter[U])(implicit sparkSession: SparkSession): DataStreamWriter[U] = {
    import appConfig._
    val conf = sparkSession.conf

    val QueryName = conf.get(sinkQueryName, SinkQueryNameDefault)
    val SinkOutputMode = conf.get(sinkOutputMode, "append").trim
    val SinkFormat = conf.get(sinkFormat, sinkFormatDefault)
    val SinkPartitionBy = conf.get(sinkPartitionBy, "").trim
    val SinkPartitionBySeparator = conf.get(sinkPartitionBySeparator, ",").trim
    val TriggerEnabled = conf.get(sinkTriggerEnabled, "false").toBoolean
    val TriggerType = conf.get(sinkTriggerType, TriggerProcessingTime).toLowerCase
    val ProcessingTimeInterval = conf.get(sinkProcessingInterval, SinkProcessingIntervalDefault).trim

    Seq(OutputModeOption, PartitionByOption, TriggerOption)
      .foldLeft[DataStreamWriter[U]](writer)((w, config) => {
        config match {
          case OutputModeOption => w.outputMode(SinkOutputMode)
          case PartitionByOption if SinkPartitionBy.nonEmpty =>
            // used for partitioned output (parquet data sink, fs sink, etc)
            val partitionColumns = SinkPartitionBy.split(SinkPartitionBySeparator).map(_.trim)
            if (partitionColumns.nonEmpty) w.partitionBy(partitionColumns: _*) else w
          case TriggerOption if TriggerEnabled => TriggerType match {
            case TriggerProcessingTime =>
              w.trigger(Trigger.ProcessingTime(ProcessingTimeInterval))
            case TriggerOnce =>
              w.trigger(Trigger.Once())
            case TriggerContinuous if SinkFormat.equals("kafka") =>
              // only kafka supports this type
              // processTimeInterval is reused as the continuous checkpoint interval
              w.trigger(Trigger.Continuous(ProcessingTimeInterval))
            case _ => w
          }
          case _ => w
        }
      })
      .format(SinkFormat)
      .queryName(QueryName)
  }

  /**
   * add your application logic here, using the streamReader and outputStream
   * helpers to create your StreamingQuery
   * @return
   */
  def runApp(): StreamingQuery

  /**
   * Simple Streaming App: Connects and process as soon as data is available
   * - output will be written to the distributed data lake in parquet
   */
  override def run(): Unit = {
    super.run()
    runApp()
    awaitAnyTermination()
  }

  def awaitTermination(query: StreamingQuery): Unit = {
    query.awaitTermination()
  }

  def awaitAnyTermination(): Unit = {
    sparkSession.streams.awaitAnyTermination()
  }

}
