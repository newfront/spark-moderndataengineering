package com.coffeeco.data

import org.apache.log4j.Logger
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{FloatType, IntegerType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.streaming.{DataStreamReader, DataStreamWriter, StreamingQuery, Trigger}

object SparkRedisStreamsApp extends SparkApplication {
  val logger: Logger = Logger.getLogger("com.coffeeco.data.SparkRedisStreamsApp")

  override def validateConfig()(implicit sparkSession: SparkSession): Boolean = {
    sparkConf.get("spark.app.source.stream", "").nonEmpty
  }

  lazy val inputStreamName: String = sparkConf.get(
    "spark.app.source.stream","com:coffeeco:coffee:v1:orders")
  lazy val appCheckpointLocation: String = sparkConf.get(
    "spark.app.checkpoint.location",
    s"/tmp/${sparkSession.sparkContext.appName}"
  )

  // processing triggers
  lazy val triggerEnabled: Boolean = sparkConf.getBoolean(
    "spark.app.stream.trigger.enabled", defaultValue = true)

  lazy val triggerType: String = sparkConf.get(
    "spark.app.stream.trigger.type", defaultValue = "processing")

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
      .schema(streamStruct)
  }

  /**
   * Simple Streaming App: Connects and process as soon as data is available
   * - all input will be output to the console and stored no where
   */
  override def run(): Unit = {
    super.run()

    // create the streamWriter
    val writer: DataStreamWriter[Row] = SparkRedisStreams(sparkSession)
      .transform(inputStream.load())
      .writeStream
      .queryName("orders")
      .format("console")

    startAndAwaitApp(writer.start())
  }

  /**
   * Simple Streaming App: Adds a Processing Trigger
   * @param trigger Options are
   *                - Trigger.ProcessingTime("5 seconds"),
   *                - Trigger.Continuous("30 seconds")
   *                - Trigger.Once - used for Stateful Batch
   */
  def runWithTrigger(trigger: Trigger, checkpointLocation: String): Unit = {
    super.run()

    val writer: DataStreamWriter[Row] = SparkRedisStreams(sparkSession)
      .transform(inputStream.load())
      .writeStream
      .queryName("orders")
      .option("checkpointLocation", checkpointLocation)
      .trigger(trigger)
      .format("console")

    startAndAwaitApp(writer.start())
  }

  /**
   * runOnce will run through a single tick of the system. It will read from the RedisStream.
   * It will then convert the data to a DataFrame and print that to the console
   * Before cleaning up shop, and storing the checkpoints for use on the next run.
   */
  def runOnce(): Unit = {
    val checkpointLocation = appCheckpointLocation + "trigger_once/"
    runWithTrigger(Trigger.Once(), checkpointLocation)
  }

  def runOnSchedule(duration: String = "10 seconds"): Unit = {
    val checkpointLocation = appCheckpointLocation + "trigger_schedule/"
    runWithTrigger(Trigger.ProcessingTime(duration), checkpointLocation)
  }

  def runContinuously(checkpointFrequency:String = "10 seconds"): Unit = {
    val checkpointLocation = appCheckpointLocation + "trigger_continuously/"
    runWithTrigger(Trigger.Continuous(checkpointFrequency), checkpointLocation)
  }

  // allow you to run once and keep the state around
  def startAndAwaitApp(streamingQuery: StreamingQuery): Unit = {
    streamingQuery.awaitTermination()
  }

  if (triggerEnabled) {
    triggerType match {
      case "processing" => runOnSchedule("15 seconds")
      case "once" => runOnce()
      case "continuous" => runContinuously("30 seconds")
    }
    // trigger will change the periodicity
  } else run()

}