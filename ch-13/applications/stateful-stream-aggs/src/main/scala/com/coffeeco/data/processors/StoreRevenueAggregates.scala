package com.coffeeco.data.processors

import com.coffeeco.data.traits.{DataFrameTransformer, WindowedDataFrameProcessor}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.TimestampType
import org.apache.spark.sql.{DataFrame, SparkSession}

object StoreRevenueAggregates {
  def apply(spark: SparkSession): StoreRevenueAggregates = {
    new StoreRevenueAggregates(spark)
  }
}

@SerialVersionUID(100L)
class StoreRevenueAggregates(val spark: SparkSession)
  extends DataFrameTransformer with WindowedDataFrameProcessor
    with Serializable {

  override def transform(df: DataFrame): DataFrame = {
    // the timestamp column is a BigInteger column
    // we need to transform this so it can be changed to TimestampType from BigInt
    df
      .withColumn(timestampColumn, to_timestamp(
        col(timestampColumn)
          .divide(lit(1000))
          .cast(TimestampType)))
  }

  override def process(df: DataFrame): DataFrame = {
    import spark.implicits._
    // df.isStreaming (is the way to know if the dataframe is from a DataFrameReader or a DataStreamReader

    df
      .dropDuplicates("orderId")
      .withWatermark(timestampColumn, watermarkDuration)
      .groupBy($"storeId", groupingWindow())
      .agg(
        count($"orderId") as "totalOrders",
        sum($"numItems") as "totalItems",
        bround(sum($"price"), 2) as "totalRevenue",
        percentile_approx($"numItems", lit(0.95), lit(95))
          as "numItemsP95",
        avg($"numItems") as "averageNumItems"
      )
  }

}
