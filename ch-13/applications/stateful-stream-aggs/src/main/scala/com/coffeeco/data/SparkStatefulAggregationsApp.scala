package com.coffeeco.data

import com.coffeeco.data.config.AppConfig
import com.coffeeco.data.format.CoffeeOrder
import com.coffeeco.data.processors.StoreRevenueAggregates
import com.coffeeco.data.traits.SparkStructuredStreamingApplication
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, Row, SparkSession}
import org.apache.spark.sql.streaming.{DataStreamReader, DataStreamWriter, StreamingQuery}

object SparkStatefulAggregationsApp extends SparkStructuredStreamingApplication[DataFrame, Row] {
  import AppConfig._
  val logger: Logger = Logger.getLogger("com.coffeeco.data.SparkStatefulAggregationsApp")

  override lazy val inputStream: DataStreamReader = {
    val conf = sparkSession.conf
    val SourceSchemaDDL = conf.get(sourceSchemaDDL, "").trim
    // fold additional DataStreamReader options into the mix
    Seq("schema").foldLeft[DataStreamReader](streamReader)( (reader, config) => {
      config match {
        case "schema" if SourceSchemaDDL.nonEmpty =>
          // result of calling schema.toDDL
          reader.schema(SourceSchemaDDL)
        case "schema" =>
          reader.schema(Encoders.product[CoffeeOrder].schema)
        case _ => reader
      }
    })
  }

  /**
   * Run the main Spark application
   * @return a StreamingQuery that can be used for application introspection
   */
  override def runApp(): StreamingQuery = {
    val conf = sparkSession.conf
    // 1. generates a new inputStream (DataStreamReader)
    // that uses MicroBatch processing to pass micro-batches as DataFrames
    // 2. The StoreRevenueAggregates process method
    // uses groupBy with a Window column to bucket CoffeeOrders by storeId
    // to emit periodic time-series aggregations

    val processor: StoreRevenueAggregates = StoreRevenueAggregates(sparkSession)
    val pipeline: Dataset[Row] = processor
      .transform(inputStream.load()) // DataFrame
      .transform(processor.process) // DataFrame == Dataset[Row]

    // 3. that are then emitted as an append or update stream
    // to a StreamingSink using a DataStreamWriter
    val writer = outputStream(pipeline.writeStream)

    // 4. which conditionally outputs data to a Streaming Table or simply starts
    // the StreamingQuery
    conf.get(sinkToTableName, "") match {
      case tableName if tableName.nonEmpty => writer.toTable(tableName)
      case _ => writer.start()
    }
  }

  override def validateConfig()(implicit sparkSession: SparkSession): Boolean = {
    super.validateConfig()
    // add any additional checks (could check for source, sink formats)
  }

  run()
}
