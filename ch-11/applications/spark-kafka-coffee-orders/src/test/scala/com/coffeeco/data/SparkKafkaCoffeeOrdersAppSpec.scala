package com.coffeeco.data

import org.apache.spark.SparkConf
import com.coffeeco.data.TestHelper.fullPath
import org.apache.spark.sql.{Encoder, Encoders}
import org.apache.spark.sql.execution.streaming.MemoryStream
import org.apache.spark.sql.streaming.OutputMode
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SparkKafkaCoffeeOrdersAppSpec extends AnyFlatSpec
  with Matchers with SharedSparkSql {
  import TestHelper.Kafka._

  override def conf: SparkConf = {
    val sparkWarehouseDir = fullPath(
      "src/test/resources/spark-warehouse")

    val testConfigPath = fullPath(
      "src/test/resources/application-test.conf")

    // override the location of the config to our testing config
    sys.props += ( ("config.file", testConfigPath ) )

    SparkKafkaCoffeeOrdersApp.sparkConf
      .setMaster("local[*]")
      .set("spark.app.id", appID)
      .set("spark.sql.warehouse.dir", sparkWarehouseDir)
  }

  val kafkaDataFrameEncoder: Encoder[MockKafkaDataFrame] = Encoders.product[MockKafkaDataFrame]

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  "SparkKafkaCoffeeOrdersApp" should " generate CoffeeOrder data and produce kafka records" in {
    val testSession = SparkKafkaCoffeeOrdersApp.sparkSession
    import scalapb.spark.Implicits._
    import scalapb.json4s.JsonFormat
    import com.coffeeco.protocol.coffee.common._

    val order = CoffeeOrder(
      timestamp = 1625766472513L,
      orderId = "ord126",
      storeId = "st1",
      customerId = "ca183",
      numItems = 6,
      price = 48.00f
    )

    val orderJson: String = JsonFormat.toJsonString(order)
    val jsonToCoffeeOrderProto: CoffeeOrder = JsonFormat.fromJsonString[CoffeeOrder](orderJson)

    jsonToCoffeeOrderProto shouldEqual order

    val memoryStream = new MemoryStream[MockKafkaDataFrame](
      id = 1,
      sqlContext = testSession.sqlContext,
      numPartitions = Some(4)
    )(kafkaDataFrameEncoder)

    val orderTransformer = KafkaOrderTransformer(testSession)

    val streamingQuery = orderTransformer
      .transform(memoryStream.toDF())
      .map(CoffeeOrder.parseFrom)
      .writeStream
      .format("memory")
      .queryName("coffee_orders")
      .outputMode(OutputMode.Append())
      .start()

    // add one record (like emitting one Kafka record)
    memoryStream.addData(Seq(
      MockKafkaDataFrame(
        key = order.orderId.getBytes(),
        value = order.toByteArray
      )
    ))

    streamingQuery.processAllAvailable()

    testSession.sql("select * from coffee_orders").show()

    /*
    +-------------+--------+--------+-----------+---------+-----+
    |    timestamp|order_id|store_id|customer_id|num_items|price|
    +-------------+--------+--------+-----------+---------+-----+
    |1625766472513|  ord126|     st1|      ca183|        6| 48.0|
    +-------------+--------+--------+-----------+---------+-----+
     */

    streamingQuery.stop()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    // add anything else you may need to clean up or track after the suite is complete
  }

}
