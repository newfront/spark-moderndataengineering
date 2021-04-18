package com.coffeeco.data.config

import com.typesafe.config.ConfigFactory
import scala.collection.convert.ImplicitConversions.`map AsScala`

object Configuration {
  /* Updating the config location can be done with this override -Dconfig.file=path/to/config-file */
  private lazy val defaultConfig = ConfigFactory.load("application.conf")
  private val config = ConfigFactory.load().withFallback(defaultConfig)

  config.checkValid(ConfigFactory.defaultReference(), "default")

  private lazy val root = config.getConfig("default")
  lazy val appName: String = root.getString("appName")

  object Spark {
    private val spark = root.getConfig("spark")
    private val _settings = spark.getObject("settings")
    lazy val settings: Map[String, String] = _settings.map({ case (k,v) =>
      (k, v.unwrapped().toString)
    }).toMap
  }

}
