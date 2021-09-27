package com.coffeeco.data

import com.coffeeco.data.config.AppConfig
import com.coffeeco.data.traits.SparkStructuredStreamingApplication
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.types.{FloatType, IntegerType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.streaming.DataStreamReader

object SparkStatefulAggregationsApp extends SparkStructuredStreamingApplication[DataFrame, Row] {
  import org.apache.spark.sql.functions._
  val logger: Logger = Logger.getLogger("com.coffeeco.data.SparkStatefulAggregationsApp")

  override def validateConfig()(implicit sparkSession: SparkSession): Boolean = {
    super.validateConfig()
    // add any additional checks (could check for source, sink formats)
  }

  // inputDF
  // stream data source format for reading
  lazy val streamStruct: StructType = new StructType()
    .add(StructField("timestamp", LongType, nullable = false))
    .add(StructField("orderId", StringType, nullable = false))
    .add(StructField("storeId", StringType, nullable = false))
    .add(StructField("customerId", StringType, nullable = false))
    .add(StructField("numItems", IntegerType, nullable = false))
    .add(StructField("price", FloatType, nullable = false))

  // add additional sup
  override lazy val inputStream: DataStreamReader = {
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

  /**
   * Simple Streaming App: Connects and process as soon as data is available
   * - output will be written to the distributed data lake in parquet
   */
  override def run(): Unit = {
    import sparkSession.implicits._
    super.run()

    // 1. generates a new inputStream (DataStreamReader)
    // that uses MicroBatch processing to pass micro-batches as DataFrames
    // 2. The StoreRevenueAggregates process method
    // uses groupBy with a Window column to bucket CoffeeOrders by storeId
    // to emit periodic time-series aggregations

    val processor = StoreRevenueAggregates(sparkSession)
    val aggregationPipeline: Dataset[Row] = processor
      .transform(inputStream.load())
      .transform(processor.process)

    // 3. that are then emitted as an append or update stream
    // to a StreamingSink using a DataStreamWriter
    val writer = outputStream(aggregationPipeline.writeStream)

    // 4. which conditionally outputs data to a Streaming Table or simply starts
    // the StreamingQuery
    val streamingQuery = sparkSession.conf.get(AppConfig.sinkToTableName, "") match {
      case tableName if tableName.nonEmpty =>
        writer.toTable(tableName)
      case _ =>
        writer.start()
    }

    startAndAwaitApp(streamingQuery)
  }

  run()
}