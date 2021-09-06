package com.coffeeco.data.generators

import com.coffeeco.data.SparkApplication
import com.coffeeco.protocol.coffee.common.{CoffeeOrder, KafkaRecord}
import com.google.protobuf.ByteString
import org.apache.spark.sql.SparkSession

import scala.util.Random

object CoffeeOrderGenerator extends SparkApplication {
  import java.time._
  import scalapb.spark.Implicits._

  lazy val KafkaBootstrapServers: String = sparkConf.get(
    "spark.app.sink.option.kafka.bootstrap.servers",
    "127.0.0.1:9093, 127.0.0.1:9094, 127.0.0.1:9095")

  lazy val KafkaTopic = sparkConf.get(
    "spark.app.sink.kafka.topic",
    "com.coffeeco.coffee.v1.orders")

  lazy val TotalRecords = sparkConf.getInt("spark.data.generator.totalRecords", 10)
  lazy val IndexOffset = sparkConf.getInt("spark.data.generator.indexOffset", 0);

  override val args: Array[String] = CoffeeOrderGenerator.super.args
  /**
   * ensure that the application can run correctly, and there is no missing or empty config
   *
   * @param sparkSession The SparkSession
   * @return true if the application is okay to start
   */
  override def validateConfig()(implicit sparkSession: SparkSession): Boolean = {
    true
  }

  lazy val BoundRandom = {
    val seed: Int = 300
    new Random(seed)
  }

  // we want to use the command line arguments to modify and randomize the values in the
  // CoffeeOrder data objects
  def generateCoffeeOrder(
    from: Instant,
    to: Instant,
    totalRecords: Int = TotalRecords,
    indexOffset: Int = IndexOffset): Seq[CoffeeOrder] = {
    // totalSales is a distribution of the number of items purchased
    // from the startTime (from: Instant) until the (to: Instant) - aka the Range),
    // totalSales can help us achieve a time granularity between sales

    val random = new Random(totalRecords)
    val stepSize = (to.toEpochMilli - from.toEpochMilli)/1000
    // calculate the time for each coffee order
    (0 to totalRecords).map { index =>
      CoffeeOrder(
        timestamp = from.plusMillis(index*stepSize).toEpochMilli,
        orderId = s"orderId${indexOffset+index+1}",
        storeId = s"store${random.nextInt(4)+1}",
        customerId = s"cust${random.nextInt(100)+1}",
        numItems = BoundRandom.nextInt(20)+1,
        price = BoundRandom.nextFloat()+1.0f
      )
    }
  }

  override def run(): Unit = {
    import sparkSession.implicits.StringToColumn
    // generate the data
    val until = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
    // generate
    val orders: Seq[CoffeeOrder] = CoffeeOrderGenerator
      .generateCoffeeOrder(
        from = until.minusHours(8).toInstant(ZoneOffset.UTC),
        to = until.toInstant(ZoneOffset.UTC)
    )

    val orderEvents = sparkSession.createDataset[CoffeeOrder](orders).map { order =>
      KafkaRecord(ByteString.copyFrom(order.orderId.getBytes),
        order.toByteString, KafkaTopic)
    }

    orderEvents
      .select($"key",$"value",$"topic")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", KafkaBootstrapServers)
      .save()

    /*
    // alternative writer declares the Kafka topic using the DataFrameWriter properties `topic` on the Kafka DataSource
    orderEvents
      .select($"key",$"value")
      .write
      .format("kafka")
      .option("kafka.bootstrap.servers", KafkaBootstrapServers)
      .option("topic", KafkaTopic)
      .save()
     */
  }

  run()
}
