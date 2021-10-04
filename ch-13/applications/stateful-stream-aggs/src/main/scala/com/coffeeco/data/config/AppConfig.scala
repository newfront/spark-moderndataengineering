package com.coffeeco.data.config

import com.coffeeco.data.traits.SparkAppConfig

object AppConfig extends SparkAppConfig {

  override val sourceFormatDefault = "redis"
  // windowing support
  override val windowTimestampColumnDefault = "timestamp"

  override val DefaultWindowDuration: String = "15 minutes"
  override val DefaultWatermarkDuration: String = "5 minutes"
}
