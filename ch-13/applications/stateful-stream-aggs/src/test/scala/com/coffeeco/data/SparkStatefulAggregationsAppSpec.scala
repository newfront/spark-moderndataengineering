package com.coffeeco.data

import com.coffeeco.data.config.AppConfig
import com.coffeeco.data.format.CoffeeOrder
import com.coffeeco.data.processors.StoreRevenueAggregates
import org.apache.spark.sql.{Row, SQLContext, SparkSession}
import org.apache.spark.sql.execution.streaming.MemoryStream

class SparkStatefulAggregationsAppSpec extends StreamingAggregateTestBase {

  "StoreRevenueAggregates" should " produce windowed statistics" in {
    implicit val testSession: SparkSession = SparkStatefulAggregationsApp
      .sparkSession.newSession()
    val outputQueryName = "order_aggs"
    testSession.conf.set(AppConfig.sinkQueryName, outputQueryName)
    import testSession.implicits._
    import org.apache.spark.sql.functions._
    implicit val sqlContext: SQLContext = testSession.sqlContext

    // Split into 6 groups (acting like 6 micro-batches)
    val coffeeOrders = TestHelper.coffeeOrderData().grouped(6)


    /*
      val coffeeOrderStream = new MemoryStream[CoffeeOrder](
      id=0,testSession.sqlContext, numPartitions = Some(2))(coffeeOrderItemEncoder)
     */
    val coffeeOrderStream = MemoryStream[CoffeeOrder]
    coffeeOrderStream.addData(coffeeOrders.next())

    /*
    // Setting up the full aggregation for test, before leaning back onto the test-config
    // MemoryStream and MemorySink
    val streamingQuery = StoreRevenueAggregates(testSession)
      .process(coffeeOrderStream.toDF())
      .writeStream
      .format("memory")
      .queryName("order_aggs")
      .trigger(Trigger.ProcessingTime("5 seconds"))
      .outputMode(OutputMode.Append())
      .start() // batch 0 (takes the first group of coffee orders from the coffeeOrderStream
     */

    // use the config to drive the output stream
    val processor = StoreRevenueAggregates(testSession)
    val aggregationPipeline = processor
      .transform(coffeeOrderStream.toDF())
      .transform(processor.process)

    val streamingQuery = SparkStatefulAggregationsApp
      .outputStream(aggregationPipeline.writeStream)
      .start()

    // queue up all the data for processing
    coffeeOrders.foreach(orders =>
      coffeeOrderStream.addData(orders)
    )
    // tell Spark to trigger everything available
    streamingQuery.processAllAvailable()

    /*
    // As an alternative, you can trigger individual batches to watch
    // the aggregations get built up

    coffeeOrderStream.addData(coffeeOrders.next())
    streamingQuery.processAllAvailable()

    // batch 2
    coffeeOrderStream.addData(coffeeOrders.next())
    streamingQuery.processAllAvailable()

    // batch 3
    coffeeOrderStream.addData(coffeeOrders.next())
    streamingQuery.processAllAvailable()

    // batch 4
    */
    //streamingQuery.explain(extended = true)

    // adding listeners to the queries gives you a way of monitoring application progress / metrics
    val progress = streamingQuery.lastProgress
    // print the final queryProgress
    println(progress.toString())
    val result = testSession
      .sql(s"select * from $outputQueryName order by window.start, storeId asc")
      .toDF("store_id","window","orders","items","revenue","p95_items","avg_items")
    result.show()

    val resultDDL = "`store_id` STRING,`window` STRUCT<`start`: TIMESTAMP, `end`: TIMESTAMP>,`orders` BIGINT,`items` BIGINT,`revenue` DOUBLE,`p95_items` INT,`avg_items` DOUBLE"
    val row = result
      .where(col("store_id").equalTo("storeA"))
      .sort(desc("orders"))
      .collect().head

    row.getDouble(row.fieldIndex("avg_items")) shouldBe 2.0d
    row.getInt(row.fieldIndex("p95_items")) shouldBe 4
    row.getDouble(row.fieldIndex("revenue")) shouldBe 36.96d

    streamingQuery.stop()
  }

}