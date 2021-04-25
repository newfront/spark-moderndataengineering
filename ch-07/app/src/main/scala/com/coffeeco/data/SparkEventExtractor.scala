package com.coffeeco.data

import org.apache.spark.sql.{DataFrame, SparkSession}

@SerialVersionUID(100L)
case class SparkEventExtractor(spark: SparkSession)
  extends DataFrameTransformer {
  import spark.implicits._

  // follow up: add configuration for the customers join table

  override def transform(df: DataFrame): DataFrame = df
    .filter($"eventType".equalTo("CustomerRatingEventType"))
    .transform(withCustomerData)

  def withCustomerData(df: DataFrame): DataFrame = {
    if (spark.catalog.tableExists("customers")) {
      spark.table("customers")
        .select($"customerId",
          $"firstName",
          $"lastName",
          $"email",
          $"created".as("joined")
        )
        .join(df, usingColumn = "customerId")
    } else throw new RuntimeException("Missing Table: customers")
  }
}
