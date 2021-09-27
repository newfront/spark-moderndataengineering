package com.coffeeco.data

import com.coffeeco.data.config.AppConfig
import com.coffeeco.data.format.{CoffeeOrder, CoffeeOrderStats}
import com.coffeeco.data.traits.SparkStructuredStreamingApplication
import org.apache.log4j.Logger
import org.apache.spark.sql.streaming.DataStreamReader
import org.apache.spark.sql.types.{FloatType, IntegerType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.Dataset

object SparkTypedStatefulAggregationsApp
  extends SparkStructuredStreamingApplication[Dataset[CoffeeOrder], CoffeeOrderStats] {
  val logger: Logger = Logger.getLogger("com.coffeeco.data.SparkTypedStatefulAggregationsApp")

  override lazy val inputStream: DataStreamReader = {

    val streamStruct: StructType = new StructType()
      .add(StructField("timestamp", LongType, nullable = false))
      .add(StructField("orderId", StringType, nullable = false))
      .add(StructField("storeId", StringType, nullable = false))
      .add(StructField("customerId", StringType, nullable = false))
      .add(StructField("numItems", IntegerType, nullable = false))
      .add(StructField("price", FloatType, nullable = false))

    // fold additional DataStreamReader options into the mix
    Seq("schema").foldLeft[DataStreamReader](streamReader)( (reader, config) => {
      config match {
        case "schema" if sparkSession.conf.get(AppConfig.sourceSchemaDDL, "").nonEmpty =>
          reader.schema(sparkSession.conf.get(AppConfig.sourceSchemaDDL))
        case "schema" =>
          reader.schema(streamStruct)
        case _ => reader
      }
    })
  }

  override def run(): Unit = {
    import sparkSession.implicits._
    super.run()

    val processor = TypedRevenueAggregates(sparkSession)
    val aggregationPipeline: Dataset[CoffeeOrderStats] = processor
      .transform(inputStream.load().as[CoffeeOrder]) // Dataset[CoffeeOrderForAnalysis]
      .transform(processor.process) // Dataset[CoffeeOrderStats]

    val streamingQuery = sparkSession.conf.get(AppConfig.sinkToTableName, "") match {
      case tableName if tableName.nonEmpty =>
        outputStream(aggregationPipeline.writeStream).toTable(tableName)
      case _ =>
        outputStream(aggregationPipeline.writeStream).start()
    }

    startAndAwaitApp(streamingQuery)
  }

  run()



}
