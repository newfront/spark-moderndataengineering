package com.coffeeco.data

import org.apache.spark.sql.{DataFrame, Encoder, Encoders, SaveMode, SparkSession}

import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

object TestHelper {

  // add common helpers methods here.

  def fullPath(filePath: String): String = new java.io.File(filePath).getAbsolutePath

}
