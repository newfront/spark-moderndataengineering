package com.coffeeco.data

import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types.{FloatType, IntegerType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.streaming.{DataStreamReader, DataStreamWriter}

object SparkRedisStreamsApp extends SparkApplication {
  val logger: Logger = Logger.getLogger("com.coffeeco.data.SparkRedisStreamsApp")

  override def validateConfig()(implicit sparkSession: SparkSession): Boolean = {
    true
  }

  lazy val inputStreamName: String = sparkConf.get("spark.app.source.stream", "com:coffeeco:coffee:v1:orders")
  lazy val streamStruct: StructType = new StructType()
    .add(StructField("timestamp", LongType, nullable = false))
    .add(StructField("orderId", StringType, nullable = false))
    .add(StructField("storeId", StringType, nullable = false))
    .add(StructField("customerId", StringType, nullable = false))
    .add(StructField("numItems", IntegerType, nullable = false))
    .add(StructField("price", FloatType, nullable = false))

  lazy val inputStream: DataStreamReader = {
    sparkSession.readStream
      .format("redis")
      .option("stream.keys", inputStreamName)
      .schema(streamStruct)
  }

  override def run(): Unit = {
    super.run()

    // create the streamWriter
    val writer: DataStreamWriter[Row] = SparkRedisStreams(sparkSession)
      .transform(inputStream.load())
      .writeStream
      .queryName("orders")
      .format("console")

    // start the streaming query
    val streamingQuery = writer.start()

    // wait for the application to be terminated
    streamingQuery.awaitTermination()
  }

  run()

}