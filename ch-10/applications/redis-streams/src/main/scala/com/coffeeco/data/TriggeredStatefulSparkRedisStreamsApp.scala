package com.coffeeco.data

import com.coffeeco.data.StatefulSparkRedisStreamsApp.appCheckpointLocation
import org.apache.log4j.Logger
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{FloatType, IntegerType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.streaming.{DataStreamReader, DataStreamWriter, OutputMode, StreamingQuery, Trigger}

object TriggeredStatefulSparkRedisStreamsApp extends SparkApplication {
  val logger: Logger = Logger.getLogger("com.coffeeco.data.TriggeredStatefulSparkRedisStreamsApp")

  lazy val inputStreamName: String = sparkConf.get("spark.app.source.stream")
  lazy val appCheckpointLocation: String = sparkConf.get("spark.app.checkpoint.location")
  
  lazy val streamingTableName: String = sparkConf.get("spark.app.streaming.table.name", "coffee_orders")
  lazy val streamingSinkLocation: String = sparkConf.get("spark.app.streaming.sink.path")

  // processing triggers
  lazy val triggerEnabled: Boolean = sparkConf.getBoolean(
    "spark.app.stream.trigger.enabled", defaultValue = true)

  lazy val triggerType: String = sparkConf.get(
    "spark.app.stream.trigger.type", defaultValue = "processing")

  override def validateConfig()(implicit sparkSession: SparkSession): Boolean = {
    if (inputStreamName.isEmpty || appCheckpointLocation.isEmpty || streamingSinkLocation.isEmpty) {
      throw new RuntimeException("The config settings spark.app.source.stream " +
        ", spark.app.checkpoint.location or the spark.app.streaming.sink.path can not be empty")
    }
    true
  }

  // stream data source format for reading
  lazy val streamStruct: StructType = new StructType()
    .add(StructField("timestamp", LongType, nullable = false))
    .add(StructField("orderId", StringType, nullable = false))
    .add(StructField("storeId", StringType, nullable = false))
    .add(StructField("customerId", StringType, nullable = false))
    .add(StructField("numItems", IntegerType, nullable = false))
    .add(StructField("price", FloatType, nullable = false))

  // data stream source reader
  lazy val inputStream: DataStreamReader = {
    sparkSession.readStream
      .format("redis")
      .option("stream.keys", inputStreamName)
      .option("stream.read.batch.size", 100) // will read only 100 items per batch
      .option("stream.read.block", 1000) // will block waiting for new events for up to a second
      .schema(streamStruct)
  }

  // data stream writer
  lazy val outputStream: DataStreamWriter[Row] = {
    SparkRedisStreams(sparkSession)
      .transform(inputStream.load())
      .writeStream
      .format("parquet")
      .queryName("orders")
      .outputMode(OutputMode.Append())
      .partitionBy("storeId")
      .option("path", streamingSinkLocation)
  }

  /**
   * Simple Streaming App: Connects and process as soon as data is available
   * - output will be written to the distributed data lake in parquet
   */
  override def run(): Unit = {
    super.run()
    val writer: DataStreamWriter[Row] = triggerType match {
      case "continuous" if triggerEnabled =>
        val checkpointInterval = "30 seconds"
        outputStream
          .option("checkpointLocation", s"$appCheckpointLocation/trigger/continuously/")
          .trigger(Trigger.Continuous(checkpointInterval))

      case "once" if triggerEnabled =>
        outputStream
          .option("checkpointLocation",
          s"$appCheckpointLocation/trigger/once/")
          .trigger(Trigger.Once())
      
      case "processing" if triggerEnabled =>
        outputStream
          .option("checkpointLocation",
            s"$appCheckpointLocation/trigger/processing_time/")
          .trigger(Trigger.ProcessingTime("10 seconds"))
      
      case _ => outputStream
        .option("checkpointLocation", s"$appCheckpointLocation/trigger/none/")
    }

    startAndAwaitApp(writer.toTable(streamingTableName))
  }

  def startAndAwaitApp(query: StreamingQuery): Unit = {
    query.awaitTermination()
  }

  run()
}