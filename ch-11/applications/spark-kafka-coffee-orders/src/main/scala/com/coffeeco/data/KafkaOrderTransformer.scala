package com.coffeeco.data

import org.apache.spark.sql.{DataFrame, Dataset, Encoder, Encoders, SparkSession}

object KafkaOrderTransformer {
  def apply(spark: SparkSession): KafkaOrderTransformer = {
    new KafkaOrderTransformer(spark)
  }
}

@SerialVersionUID(100L)
class KafkaOrderTransformer(spark: SparkSession)
  extends DatasetTransformer[Array[Byte]]
    with Serializable {

  override val encoder: Encoder[Array[Byte]] = Encoders.BINARY

  override def transform(df: DataFrame): Dataset[Array[Byte]] = {
    df.printSchema()
    df.select("value")
      .map(_.getAs[Array[Byte]]("value"))(encoder)
  }
}
