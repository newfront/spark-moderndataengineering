package com.coffeeco.data

import com.coffeeco.data.traits.{DataFrameProcessor, DataFrameTransformer, WindowedDataFrameProcessor, WindowedProcessor}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import org.apache.spark.sql.functions.{avg, col, count, lit, sum, to_timestamp, window}
import org.apache.spark.sql.types.TimestampType

object StoreRevenueAggregates {
  def apply(spark: SparkSession): StoreRevenueAggregates = {
    new StoreRevenueAggregates(spark)
  }
}

@SerialVersionUID(100L)
class StoreRevenueAggregates(val spark: SparkSession)
  extends DataFrameTransformer with WindowedDataFrameProcessor with Serializable {

  override def transform(df: DataFrame): DataFrame = {
    import spark.implicits._
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
        sum($"price") as "totalRevenue",
        avg($"numItems") as "averageNumItems"
      )
  }

}
