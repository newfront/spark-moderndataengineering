package com.coffeeco.data

import com.coffeeco.data.TestHelper.{firstOrder, tenMinutes}
import com.coffeeco.data.config.AppConfig
import com.coffeeco.data.format.CoffeeOrder
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.execution.streaming.MemoryStream
import org.apache.spark.sql.streaming.StreamingQueryStatus

import collection.JavaConverters._

class TypedRevenueAggregatesSpec extends StreamingAggregateTestBase {
  val outputQueryName = "typed_order_aggs"
  override def conf: SparkConf = {
    super.conf.set("spark.app.sink.queryName", outputQueryName)
  }
  "SparkTypedStatefulAggregationsApp" should " aggregate CoffeeOrders and produce CoffeeOrderStats across 5 minute window " in {

    // create a new session
    val testSession = SparkTypedStatefulAggregationsApp
      .sparkSession
      .newSession()

    import testSession.implicits._
    implicit val sqlContext: SQLContext = testSession.sqlContext
    // set the override output table name
    testSession.conf.set(AppConfig.sinkQueryName, outputQueryName)

    val coffeeOrders = TestHelper.coffeeOrderData().grouped(6)
    val coffeeOrderStream = MemoryStream[CoffeeOrder]
    coffeeOrderStream.addData(coffeeOrders.next())

    // use the config to drive the output stream
    val processor = TypedRevenueAggregates(testSession)
    val aggregationPipeline = coffeeOrderStream
      .toDF()
      .as[CoffeeOrder]
      .transform(processor.transform)
      .transform(processor.process)

    val streamingQuery = SparkTypedStatefulAggregationsApp
      .outputStream(aggregationPipeline.writeStream)(testSession).start()

    // queue up all the data for processing
    coffeeOrders.foreach(orders =>
      coffeeOrderStream.addData(orders)
    )
    // tell Spark to trigger everything available
    streamingQuery.processAllAvailable()

    val currentProgress = streamingQuery.lastProgress
    val queryDetails = currentProgress.eventTime.asScala

    // "watermark" : "2021-09-25T06:16:00.000Z"
    // push two more items into batch 3
    coffeeOrderStream.addData(
      Seq(
        CoffeeOrder(firstOrder.plusSeconds(tenMinutes*3).toEpochMilli, "orderN1", "storeG", "custbc1", 2, 6.89f),
        CoffeeOrder(firstOrder.plusSeconds(tenMinutes*3+60).toEpochMilli, "orderN2", "storeG", "custbc2", 1, 4.89f)
      )
    )
    // we will now kick off batch 3
    streamingQuery.processAllAvailable()

    coffeeOrderStream.addData(
      Seq(
        // take a record from a day ago
        CoffeeOrder(firstOrder.minusSeconds(86400L).toEpochMilli, "orderA23aa", "storeBB", "cust626", 1, 29.99f),
        CoffeeOrder(firstOrder.plusSeconds(tenMinutes*4).toEpochMilli, "orderN3", "storeA", "custB2b", 5, 22.44f)
      )
    )

    // adding listeners to the queries gives you a way of monitoring application progress / metrics
    val progress = streamingQuery.lastProgress
    // print the final queryProgress
    println(progress.toString())
    // pushed the "watermark" : "2021-09-25T06:26:00.000Z"
    // what is this end to end query doing?
    streamingQuery.explain()

    val status: StreamingQueryStatus = streamingQuery.status
    //status.message Waiting for data to arrive
    //status.isTriggerActive true
    //status.isDataAvailable false
    val result = testSession.sql(s"select * from $outputQueryName order by window.start, storeId asc")
    result.show(truncate = false)

    // Now you can test the assumptions about your DataFrame
    // -
    streamingQuery.stop()
  }

}
