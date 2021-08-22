package com.coffeeco.data

import com.coffeeco.protocol.coffee.common.CoffeeOrder
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{DataStreamReader, DataStreamWriter, StreamingQuery}

object SparkKafkaCoffeeOrdersApp extends SparkApplication {
  import scalapb.spark.Implicits._
  val logger: Logger = Logger.getLogger("com.coffeeco.data.SparkKafkaCoffeeOrdersApp")
  
  override def validateConfig()(implicit sparkSession: SparkSession): Boolean = { true }
  
  lazy val kafkaBootstrapServers: String = sparkConf.get(
    "spark.app.sink.option.kafka.bootstrap.servers",
    "127.0.0.1:9093,127.0.0.1:9094,127.0.0.1:9095")
  
  lazy val kafkaSubscription = sparkConf.get(
    "spark.app.sink.option.subscribe",
    "com.coffeeco.coffee.v1.orders")

  lazy val inputStream: DataStreamReader = {
    sparkSession.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServers)
      .option("subscribe", kafkaSubscription)
  }
  
  override def run(): Unit = {
    super.run()
    
    val writer: DataStreamWriter[CoffeeOrder] = KafkaOrderTransformer(sparkSession)
      .transform(inputStream.load())
      .map(CoffeeOrder.parseFrom)
      .as[CoffeeOrder]
      .writeStream
      .queryName("orders")
      .format("console")

    startAndAwaitApp(writer.start())
  }

  def startAndAwaitApp(query: StreamingQuery): Unit = {
    query.awaitTermination()
  }

  run()

}
