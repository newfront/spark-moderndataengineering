package com.coffeeco.data.traits

import org.apache.spark.sql.DataFrame

trait DataFrameTransformer {
  def transform(df: DataFrame): DataFrame
}
