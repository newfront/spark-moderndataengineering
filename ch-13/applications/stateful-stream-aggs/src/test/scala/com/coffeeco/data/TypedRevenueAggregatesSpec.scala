package com.coffeeco.data

import com.coffeeco.data.TestHelper.firstOrder
import com.coffeeco.data.config.AppConfig
import com.coffeeco.data.format.CoffeeOrder
import com.coffeeco.data.listeners.{QueryListener, SparkApplicationListener}
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Dataset, SQLContext, SaveMode, SparkSession}
import org.apache.spark.sql.execution.streaming.MemoryStream
import org.apache.spark.sql.streaming.StreamingQueryStatus

import java.lang
import java.time.temporal.ChronoUnit.{HOURS, MINUTES}

class TypedRevenueAggregatesSpec extends StreamingAggregateTestBase {

  val logger: Logger = Logger.getLogger(classOf[TypedRevenueAggregatesSpec])

  val outputQueryName = "typed_order_aggs"
  override def conf: SparkConf = {
    super.conf.set("spark.app.sink.queryName", outputQueryName)
  }
  "SparkTypedStatefulAggregationsApp" should " aggregate CoffeeOrders and produce CoffeeOrderStats across 5 minute window " in {

    // create a new session
    val testSession = SparkTypedStatefulAggregationsApp
      .sparkSession
      .newSession()

    // Enable the SparkListener to add breakpoints via the SparkApplicationListener class
    // This listener can be used to tap into Spark's control flow and operational metrics events programmatically

    testSession.sparkContext.addSparkListener(SparkApplicationListener())

    // Enable the StreamingQueryListener to add breakpoints via the QueryListener case class
    // This will simply log values by default, but you can use these events (on the Spark driver)
    // to add intelligence layers over your Spark applications, send custom metrics, or integrate with Slack or
    // PagerDuty

    testSession.streams.addListener(QueryListener())

    // switch between default and queued to use the FAIR vs FIFO scheduler
    testSession.sparkContext.setLocalProperty("spark.scheduler.pool", "default")


    import testSession.implicits._
    implicit val sqlContext: SQLContext = testSession.sqlContext
    // set the override output table name
    testSession.conf.set(AppConfig.sinkQueryName, outputQueryName)
    testSession.conf.set(AppConfig.sinkOutputMode, "append")

    val coffeeOrders = TestHelper.coffeeOrderData()
      .grouped(6) // split the sequence by sets of 6 orders

    val coffeeOrderStream = MemoryStream[CoffeeOrder]
    coffeeOrderStream.addData(coffeeOrders.next())

    // use the config to drive the output stream
    val processor = TypedRevenueAggregates(testSession)
    val aggregationPipeline = coffeeOrderStream
      .toDS()
      .transform(processor.transform)
      .transform(processor.process)

    val streamingQuery = SparkTypedStatefulAggregationsApp
      .outputStream(aggregationPipeline.writeStream)(testSession)
      .start()

    new Thread {
      val secondSession: SparkSession = testSession.newSession()
      secondSession.sparkContext.setLocalProperty("spark.scheduler.pool", "queued")
      secondSession.sparkContext.setJobGroup("batch.job", "runs in thread", true)
      val df: Dataset[lang.Long] = secondSession.range(100)
      df.write.mode(SaveMode.Overwrite).format(source = "console").save()
      secondSession.sparkContext.setLocalProperty("spark.scheduler.pool", null)
    }

    // queue the data for processing
    // and manually trigger spark
    coffeeOrders.foreach(orders => {
      coffeeOrderStream.addData(orders)
    })
    //streamingQuery.processAllAvailable() // force next micro-batch

    val currentProgress = streamingQuery.lastProgress
    //val queryDetails = currentProgress.eventTime.asScala

    // "watermark" : "2021-09-25T06:16:00.000Z"
    // push two more items into batch 3
    coffeeOrderStream.addData(
      Seq(
        CoffeeOrder(firstOrder.plus(26, MINUTES).toEpochMilli, "orderN1", "storeG", "custbc1", 2, 6.89f),
        CoffeeOrder(firstOrder.plus(27, MINUTES).toEpochMilli, "orderN2", "storeG", "custbc2", 1, 4.89f),
        CoffeeOrder(firstOrder.plus(35, MINUTES).toEpochMilli, "orderN3", "storeA", "custbc3", 2, 9.99f),
        CoffeeOrder(firstOrder.plus(50, MINUTES).toEpochMilli, "orderN4", "storeA", "custbc6", 3, 19.99f),
        CoffeeOrder(firstOrder.minus(12, HOURS).toEpochMilli, "orderA23aa", "storeBB", "cust626", 1, 29.99f),
        CoffeeOrder(firstOrder.plus(1, HOURS).toEpochMilli, "orderN3", "storeA", "custB2b", 5, 22.44f)
      )
    )
    // we will now kick off batch 3
    streamingQuery.processAllAvailable()

    // adding listeners to the queries gives you a way of monitoring application progress / metrics
    //val progress = streamingQuery.lastProgress
    //println(progress.toString())

    streamingQuery.explain()

    val status: StreamingQueryStatus = streamingQuery.status
    //status.message Waiting for data to arrive
    //status.isTriggerActive
    //status.isDataAvailable
    val result = testSession.sql(s"select * from $outputQueryName order by window.start, storeId asc")
    result.show()
    //val jsonResults = result.toJSON.collect

    streamingQuery.stop()
  }

}