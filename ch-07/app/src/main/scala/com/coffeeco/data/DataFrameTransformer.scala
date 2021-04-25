package com.coffeeco.data

import org.apache.spark.sql.DataFrame

trait DataFrameTransformer {
  def transform(df: DataFrame): DataFrame
}
