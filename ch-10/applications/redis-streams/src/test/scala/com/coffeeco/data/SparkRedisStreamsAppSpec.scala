package com.coffeeco.data

import com.coffeeco.data.SparkRedisStreamsApp.{inputStream, sparkSession}
import com.coffeeco.data.TestHelper.fullPath
import org.apache.spark.SparkConf
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SparkRedisStreamsAppSpec extends AnyFlatSpec
  with Matchers with SharedSparkSql {

  override def conf: SparkConf = {
    val sparkWarehouseDir = fullPath(
      "src/test/resources/spark-warehouse")

    val testConfigPath = fullPath(
      "src/test/resources/application-test.conf")

    // override the location of the config to our testing config
    sys.props += ( ("config.file", testConfigPath ))

    SparkRedisStreamsApp.sparkConf
      .setMaster("local[*]")
      .set("spark.app.id", appID)
      .set("spark.sql.warehouse.dir", sparkWarehouseDir)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  "SparkRedisStreamsApp" should " read a local stream " in {
    val testSession = SparkRedisStreamsApp.sparkSession
    import testSession.implicits._

    // note: requires you to be running the redis docker container (see the docker-compose)
    val streamingQuery = SparkRedisStreams(sparkSession)
      .transform(inputStream.load())
      .writeStream
      .queryName("orders")
      .format("console")
      .start()

    streamingQuery.processAllAvailable() // will trigger a batch
    streamingQuery.awaitTermination(5000) // will wait for 5 seconds before shutting down the app

  }

  override def afterAll(): Unit = {
    super.afterAll()
    // add anything else you may need to clean up or track after the suite is complete
  }

}
