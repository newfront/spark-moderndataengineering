package com.coffeeco.data

import com.coffeeco.data.config.AppConfig
import com.coffeeco.data.format.{CoffeeOrder, CoffeeOrderForAnalysis, CoffeeOrderStats}
import com.coffeeco.data.processors.TypedRevenueAggregates
import com.coffeeco.data.traits.SparkStructuredStreamingApplication
import org.apache.log4j.Logger
import org.apache.spark.sql.streaming.{DataStreamReader, StreamingQuery}
import org.apache.spark.sql.{Dataset, Encoders}

object SparkTypedStatefulAggregationsApp
  extends SparkStructuredStreamingApplication[
    Dataset[CoffeeOrderForAnalysis], CoffeeOrderStats] {
  import AppConfig._
  val logger: Logger = Logger.getLogger("com.coffeeco.data.SparkTypedStatefulAggregationsApp")

  override lazy val inputStream: DataStreamReader = streamReader
    .schema(Encoders.product[CoffeeOrder].schema)

  override def runApp(): StreamingQuery = {
    import sparkSession.implicits._
    val conf = sparkSession.conf

    val processor = TypedRevenueAggregates(sparkSession)
    val pipeline: Dataset[CoffeeOrderStats] = processor
      .transform(inputStream.load().as[CoffeeOrder]) // Dataset[CoffeeOrderForAnalysis]
      .transform(processor.process) // Dataset[CoffeeOrderStats]

    conf.get(sinkToTableName, "") match {
      case tableName if tableName.nonEmpty =>
        outputStream(pipeline.writeStream)
          .toTable(tableName)
      case _ =>
        outputStream(pipeline.writeStream).start()
    }
  }

  run()
}
