package com.coffeeco.data

import org.apache.spark.sql.SaveMode

trait SparkBatchApplication extends SparkApplication {
  /**
   * Batch applications using the DataFrameWriter have the following options:
   * ErrorIfExists, Ignore, Overwrite, Append?
   * ensure you protect your endpoints. You must override the saveMode explicitly
   */
  lazy val saveMode: SaveMode = SaveMode.ErrorIfExists

  /**
   * applications should have common behavior, they all run
   * @param saveMode How should the DataFrameWriter operate? ErrorIfExists, Ignore, Overwrite, Append?
   */
  def runBatch(saveMode: SaveMode = saveMode): Unit

  final override def run(): Unit = {
    super.run()
    // if we have a valid config, continue
    runBatch()
  }

}
