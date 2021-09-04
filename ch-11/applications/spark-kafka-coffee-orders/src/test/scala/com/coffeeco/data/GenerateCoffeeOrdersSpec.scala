package com.coffeeco.data

import com.coffeeco.data.TestHelper.fullPath
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.coffeeco.data.generators.CoffeeOrderGenerator
import org.apache.spark.SparkConf

class GenerateCoffeeOrdersSpec extends AnyFlatSpec with Matchers with SharedSparkSql {

  override def conf: SparkConf = {
    val sparkWarehouseDir = fullPath(
      "src/test/resources/spark-warehouse")

    val testConfigPath = fullPath(
      "src/test/resources/application-test.conf")

    // override the location of the config to our testing config
    sys.props += ( ("config.file", testConfigPath ) )

    SparkKafkaCoffeeOrdersApp.sparkConf
      .setMaster("local[*]")
      .set("spark.app.id", appID)
      .set("spark.sql.warehouse.dir", sparkWarehouseDir)
      .set("spark.data.generator.totalRecords", "10")
      .set("spark.data.generator.indexOffset", "1000")
  }

  "CoffeeOrderGenerator" should " Make some coffee orders" in {
    // Kafka must be running locally
    CoffeeOrderGenerator.run()
  }

}
