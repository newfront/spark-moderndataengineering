package com.coffeeco.data

import org.apache.spark.sql.{DataFrame, RuntimeConfig, SparkSession}

object SparkEventExtractor {

  def apply(spark: SparkSession): SparkEventExtractor = {
    new SparkEventExtractor(spark)
  }

}

@SerialVersionUID(100L)
class SparkEventExtractor(spark: SparkSession) extends Serializable {
  import spark.implicits._
  lazy val conf: RuntimeConfig = spark.conf

  def process(df: DataFrame): DataFrame = {
    df
      .filter($"eventType".equalTo("CustomerRatingEventType"))
      .transform(withCustomerData)
  }

  def withCustomerData(df: DataFrame): DataFrame = {
    if (spark.catalog.tableExists("customers")) {
      spark.table("customers")
        .select($"customerId", $"firstName", $"lastName", $"email", $"created".as("joined"))
        .join(df, usingColumn = "customerId")
    } else throw new RuntimeException("Missing Table: customers")
  }

}


