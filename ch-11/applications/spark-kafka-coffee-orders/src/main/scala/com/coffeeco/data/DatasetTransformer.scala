package com.coffeeco.data

import org.apache.spark.sql.{DataFrame, Dataset, Encoder}

trait DatasetTransformer[T] {
  val encoder: Encoder[T]
  def transform(df: DataFrame): Dataset[T]
}
