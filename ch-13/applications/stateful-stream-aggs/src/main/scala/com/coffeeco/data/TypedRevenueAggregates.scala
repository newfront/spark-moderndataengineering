package com.coffeeco.data

import com.coffeeco.data.format.{CoffeeOrder, CoffeeOrderForAnalysis, CoffeeOrderStats, Window}
import com.coffeeco.data.traits.{DatasetTransformer, WindowedProcessor}
import org.apache.spark.sql.streaming.{GroupState, GroupStateTimeout, OutputMode}
import org.apache.spark.sql.{DataFrame, Dataset, Encoder, Encoders, SparkSession}

object TypedRevenueAggregates {
  def apply(spark: SparkSession): TypedRevenueAggregates = new TypedRevenueAggregates(spark)
}

@SerialVersionUID(111L)
class TypedRevenueAggregates(val spark: SparkSession)
  extends DatasetTransformer[CoffeeOrder, CoffeeOrderForAnalysis]
    with WindowedProcessor[Dataset[CoffeeOrderForAnalysis], Dataset[CoffeeOrderStats]] with Serializable {

  import spark.implicits._
  import org.apache.spark.sql.types._
  import org.apache.spark.sql.functions._

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
    val head = orders.next
    val stats = orders.map { order =>
      // take the count, numItems, price
      (order.numItems, order.price)
    }.foldLeft[(String,Window,Int,Int,Float,Float)]((head.storeId, head.window, 1, head.numItems, head.price, head.numItems.toFloat))( (orderStats, order) => {
      val totalOrders = orderStats._3 + 1
      val totalItems = orderStats._4 + order._1
      (
        orderStats._1, // storeId
        orderStats._2, // window
        totalOrders, // totalOrders
        totalItems, // totalItems
        orderStats._5 + order._2, //totalRevenue
        (orderStats._6 + order._1.toFloat) / 2 // averageNumItems
      )
    })
    CoffeeOrderStats(stats._1, stats._2, stats._3, stats._4, stats._5, stats._6)
  }

  def stateFunc(
    groupByKey: String,
    orders: Iterator[CoffeeOrderForAnalysis],
    state: GroupState[CoffeeOrderStats]): Iterator[CoffeeOrderStats] = {
    if (state.hasTimedOut) {
      val result = state.get
      state.remove()
      Iterator(result)
    } else {
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
            currentState.averageNumItems/currentState.totalItems + stats.averageNumItems/stats.totalItems
          )
        case None => stats
      }
      // the state needs to be updated (for the new state)
      state.update(stateHolder)
      // the timestamp must also be set (to handle timeout)
      state.setTimeoutTimestamp(stateHolder.window.end.toInstant.toEpochMilli, "30 seconds")
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
