package com.coffeeco.data

import com.coffeeco.data.TestHelper.fullPath
import org.apache.spark.SparkConf
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

abstract class StreamingAggregateTestBase extends AnyFlatSpec with Matchers with SharedSparkSql {
  override def conf: SparkConf = {
    val sparkWarehouseDir = fullPath(
      "src/test/resources/spark-warehouse")

    val testConfigPath = fullPath(
      "src/test/resources/application-test.conf")

    // override the location of the config to our testing config
    sys.props += ( ("config.file", testConfigPath ))

    SparkStatefulAggregationsApp.sparkConf
      .setMaster("local[*]")
      .set("spark.app.id", appID)
      .set("spark.sql.warehouse.dir", sparkWarehouseDir)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    // add anything else you may need to clean up or track after the suite is complete
  }

}
