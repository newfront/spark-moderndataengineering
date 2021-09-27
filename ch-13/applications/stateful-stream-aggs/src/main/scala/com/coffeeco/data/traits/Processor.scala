package com.coffeeco.data.traits

import com.coffeeco.data.config.AppConfig
import com.coffeeco.data.config.AppConfig.{DefaultWindowDuration, sourceWatermarkDuration, windowDuration, windowSlideDuration, windowStartTime}
import org.apache.spark.sql.functions.{col, window}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

sealed trait Processor[T, U] {
  val spark: SparkSession
  def process(input: T): U
}

trait WindowedProcessor[T, U] extends Processor [T, U] {

  val timestampColumn: String = spark.conf.get(AppConfig.windowTimestampColumn, "timestamp")
  val windowFrameDuration: String = spark.conf.get(windowDuration, DefaultWindowDuration)
  // watermarking in spark lets a streaming aggregation or grouped operation mark when
  // late arriving data should be ignored
  // watermarking must use the same TimestampType column as the RelationalGroupedDataset (window column)
  val watermarkDuration: String = spark.conf.get(sourceWatermarkDuration, DefaultWindowDuration)

  /**
   * Using the default config values from AppConfig generate a groupingWindow
   * This function can be applied to a DataFrame (groupBy(col(timestampColumn), groupingWindow()))
   * @return The window Column
   */
  def groupingWindow(): Column = {
    // generate the window (bucket data by time across groups as data arrives)
    val slideDuration = spark.conf.get(windowSlideDuration, windowFrameDuration)
    val startTimeOffset = spark.conf.get(windowStartTime, "0 seconds")
    // watermarking will use this same column in order to drop late arriving data
    window(col(timestampColumn), windowFrameDuration, slideDuration, startTimeOffset)
  }
}

trait DataFrameProcessor extends Processor[DataFrame, DataFrame]
trait WindowedDataFrameProcessor extends WindowedProcessor[DataFrame, DataFrame]


