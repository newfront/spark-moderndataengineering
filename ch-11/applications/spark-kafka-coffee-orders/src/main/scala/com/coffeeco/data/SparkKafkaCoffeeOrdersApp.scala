package com.coffeeco.data

import com.coffeeco.protocol.coffee.common.CoffeeOrder
import org.apache.log4j.Logger
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.sql.streaming.{DataStreamReader, DataStreamWriter, StreamingQuery, Trigger}
import org.apache.spark.sql.types.LongType

object SparkKafkaCoffeeOrdersApp extends SparkApplication {
  val logger: Logger = Logger.getLogger("com.coffeeco.data.SparkKafkaCoffeeOrdersApp")

  final val StreamConfigSourcePrefix: String = "spark.app.source"
  final val StreamConfigSinkPrefix: String = "spark.app.sink"
  final val StreamConfigAppPrefix: String = "spark.app.stream"

  // processing triggers
  lazy val triggerEnabled: Boolean = sparkConf.getBoolean(
    s"$StreamConfigAppPrefix.trigger.enabled", defaultValue = true)

  lazy val triggerType: String = sparkConf.get(
    s"$StreamConfigAppPrefix.trigger.type", defaultValue = "processing")

  // how often to checkpoint (continuous) or process (triggerInterval)
  lazy val processingInterval = sparkConf.get(s"$StreamConfigAppPrefix.processing.interval", "30 seconds")
  lazy val inputStreamFormat = sparkConf.get(s"$StreamConfigSourcePrefix.format", "kafka")
  // the output format of the stream
  lazy val outputStreamFormat = sparkConf.get(s"$StreamConfigSinkPrefix.format", "parquet")
  lazy val outputStreamQueryName = sparkConf.get(s"$StreamConfigSinkPrefix.queryName", "coffee_orders")
  lazy val outputStreamMode = sparkConf.get(s"$StreamConfigSinkPrefix.output.mode", "append")
  lazy val outputTableName = sparkConf.get(s"$StreamConfigSinkPrefix.output.tableName", "")

  override def validateConfig()(implicit sparkSession: SparkSession): Boolean = {
    // try adding validations for your configuration. If the output format is Parquet, what kind of assumptions
    // are you making about the format of the data?
    true
  }

  /**
   * Will take a config driven approach to setting up the DataStreamReader
   */
  lazy val inputStream: DataStreamReader = {
    sparkSession.readStream
      .format(inputStreamFormat)
      .options(sparkConf
        .getAllWithPrefix(s"$StreamConfigSourcePrefix.option.")
        .toMap)
  }

  /**
   * Given the input DataStream of CoffeeOrders
   * @param ds The CoffeeOrders typed Dataset
   * @return An outputStream of data
   */
  def outputStream(ds: Dataset[CoffeeOrder]): DataStreamWriter[Row] = {
    import sparkSession.implicits.StringToColumn
    import org.apache.spark.sql.functions.{to_date, to_timestamp}

    val streamOptions = sparkConf
      .getAllWithPrefix(s"$StreamConfigSinkPrefix.option.")
      .toMap

    // generate the output data stream
    val dataStream = ds
      // ScalaPB timestamp type is BigInteger, reduce to Long
      // so that the date conversion doesn't rollover
      // otherwise your date will look like `date=+53649-08-18/` which is
      // a problem!
      .withColumn("date", to_date(
        to_timestamp($"timestamp".divide(1000).cast(LongType)))
      )
      .writeStream
      .format(outputStreamFormat)
      .queryName(outputStreamQueryName)
      .outputMode(outputStreamMode)
      .partitionBy("date")
      .options(streamOptions)

    triggerType match {
      case "continuous" if triggerEnabled =>
        dataStream.trigger(Trigger.Continuous(processingInterval))
      case "once" if triggerEnabled =>
        dataStream.trigger(Trigger.Once())
      case "processing" if triggerEnabled =>
        dataStream.trigger(Trigger.ProcessingTime(processingInterval))
      case _ =>
        dataStream
    }

  }
  
  override def run(): Unit = {
    import scalapb.spark.Implicits._
    super.run()
    val inputSourceStream: Dataset[CoffeeOrder] = KafkaOrderTransformer(sparkSession)
      .transform(inputStream.load())
    val writer = outputStream(inputSourceStream)
    startAndAwaitApp(writer.start())
  }

  def startAndAwaitApp(query: StreamingQuery): Unit = {
    query.awaitTermination()
  }

  run()

}
