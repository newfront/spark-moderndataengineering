package com.coffeeco.data

import com.coffeeco.data.traits.{DataFrameProcessor, DataFrameTransformer}
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkRedisStreams {
  def apply(spark: SparkSession): SparkRedisStreams = {
    new SparkRedisStreams(spark)
  }
}

@SerialVersionUID(100L)
class SparkRedisStreams(spark: SparkSession)
  extends DataFrameTransformer with Serializable {

  override def transform(df: DataFrame): DataFrame = df

}
