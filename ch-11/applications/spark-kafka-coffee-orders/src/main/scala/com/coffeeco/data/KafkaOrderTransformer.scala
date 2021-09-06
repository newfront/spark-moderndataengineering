package com.coffeeco.data

import com.coffeeco.protocol.coffee.common.CoffeeOrder
import org.apache.spark.sql.{DataFrame, Dataset, Encoder, SparkSession}

object KafkaOrderTransformer {
  def apply(spark: SparkSession): KafkaOrderTransformer = {
    new KafkaOrderTransformer(spark)
  }
}

@SerialVersionUID(100L)
class KafkaOrderTransformer(spark: SparkSession)
  extends DatasetTransformer[CoffeeOrder]
    with Serializable {
  import scalapb.spark.Implicits._

  override val encoder: Encoder[CoffeeOrder] = typedEncoderToEncoder[CoffeeOrder]
  override def transform(df: DataFrame): Dataset[CoffeeOrder] = {
    df.printSchema()
    df
      .map(_.getAs[Array[Byte]]("value"))
      .map(CoffeeOrder.parseFrom)
      .as[CoffeeOrder]
  }
}
