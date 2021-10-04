package com.coffeeco.data.processors

import com.coffeeco.data.config.AppConfig
import com.coffeeco.data.format.{CoffeeOrder, CoffeeOrderForAnalysis, CoffeeOrderStats, Window}
import com.coffeeco.data.traits.{DatasetTransformer, SparkAppConfig, WindowedProcessor}
import org.apache.log4j.Logger
import org.apache.spark.sql.streaming.{GroupState, GroupStateTimeout, OutputMode}
import org.apache.spark.sql.{Dataset, Encoder, Encoders, SparkSession}

object TypedRevenueAggregates {
  val logger: Logger = Logger.getLogger("com.coffeeco.data.processors.TypedRevenueAggregates")
  val appConfig: SparkAppConfig = AppConfig
  def apply(spark: SparkSession): TypedRevenueAggregates = new TypedRevenueAggregates(spark)
}

@SerialVersionUID(111L)
class TypedRevenueAggregates(val spark: SparkSession)
  extends DatasetTransformer[CoffeeOrder, CoffeeOrderForAnalysis]
    with WindowedProcessor[Dataset[CoffeeOrderForAnalysis], Dataset[CoffeeOrderStats]] with Serializable {
  import TypedRevenueAggregates.logger
  import org.apache.spark.sql.functions._
  import org.apache.spark.sql.types._
  import spark.implicits._

  override implicit val inputEncoder: Encoder[CoffeeOrder] = Encoders.product[CoffeeOrder]
  override implicit val outputEncoder: Encoder[CoffeeOrderForAnalysis] = Encoders.product[CoffeeOrderForAnalysis]

  override def transform(ds: Dataset[CoffeeOrder]): Dataset[CoffeeOrderForAnalysis] = {
    ds
      .withColumn(timestampColumn,
        to_timestamp(col(timestampColumn).divide(lit(1000)).cast(TimestampType)))
      .withColumn("window", groupingWindow())
      .withColumn("key", sha1(bin(hash(col("storeId"),col("window.start"), col("window.end")))))
      .as[CoffeeOrderForAnalysis]
  }

  def orderStats(orders: Iterator[CoffeeOrderForAnalysis]): CoffeeOrderStats = {
    val head = orders.next()
    val first = (head.storeId, head.window, 1, head.numItems, head.price, head.numItems.toFloat)
    val stats =
      orders
      .map(o => (o.numItems, o.price))
      .foldLeft[(String,Window,Int,Int,Float,Float)](first)(
        (orderStats, order) => {
          val totalOrders = orderStats._3 + 1
          val totalItems = orderStats._4 + order._1
          (orderStats._1, // storeId
           orderStats._2, // window
           totalOrders, // totalOrders
           totalItems, // totalItems
           orderStats._5+order._2, //totalRevenue
           (orderStats._6+order._1.toFloat)/2) // averageNumItems
        })
    CoffeeOrderStats(stats._1, stats._2, stats._3, stats._4, stats._5, stats._6)
  }

  def stateFunc(key: String, orders: Iterator[CoffeeOrderForAnalysis],
    state: GroupState[CoffeeOrderStats]): Iterator[CoffeeOrderStats] = {
    if (state.hasTimedOut) {
      val result = state.getOption
      state.remove()
      if (result.isDefined) Iterator(result.get)
      else Iterator.empty
    } else {
      if (orders.isEmpty) return Iterator.empty
      val stats = orderStats(orders)
      val stateHolder = state.getOption match {
        case Some(currentState: CoffeeOrderStats) =>
          // merge
          CoffeeOrderStats(
            stats.storeId,
            stats.window,
            stats.totalItems + currentState.totalItems,
            stats.totalOrders + currentState.totalOrders,
            stats.totalRevenue + currentState.totalRevenue,
            //(currentState.averageNumItems + stats.averageNumItems) / 2.0 // 2.25
            currentState.averageNumItems/currentState.totalItems + stats.averageNumItems/stats.totalItems
          )
        case None => stats
      }
      // the state needs to be updated (for the new state)
      state.update(stateHolder)

      val timeout = stateHolder.window.end.toInstant.toEpochMilli
      val watermarkDelta = timeout - state.getCurrentWatermarkMs()
      logger.info(s"storeid=${stateHolder.storeId} timeout=$timeout watermarkDelta=$watermarkDelta " +
        s"processTime=${state.getCurrentProcessingTimeMs()}")

      // the timestamp must also be set (to handle timeout)
      state.setTimeoutTimestamp(timeout, "30 seconds")
      Iterator.empty
    }
  }

  override def process(ds: Dataset[CoffeeOrderForAnalysis]): Dataset[CoffeeOrderStats] = {
    ds
      .dropDuplicates("orderId")
      .withWatermark(timestampColumn, watermarkDuration)
      .groupByKey(_.key)
      .flatMapGroupsWithState[CoffeeOrderStats, CoffeeOrderStats](OutputMode.Append(), GroupStateTimeout.EventTimeTimeout())(stateFunc)
  }

}
