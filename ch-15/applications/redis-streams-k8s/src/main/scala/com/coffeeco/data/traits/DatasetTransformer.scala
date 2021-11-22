package com.coffeeco.data.traits

import org.apache.spark.sql.{Dataset, Encoder}

trait DatasetTransformer[T, U] {
  implicit val inputEncoder: Encoder[T]
  implicit val outputEncoder: Encoder[U]
  def transform(df: Dataset[T]): Dataset[U]
}

