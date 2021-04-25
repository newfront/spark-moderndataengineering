package com.coffeeco.data

import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SaveMode}
import TestHelper.fullPath
import com.coffeeco.data.SparkEventExtractorApp.Conf.{DestinationTableName, SaveModeName, SourceTableName}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class SparkEventExtractorSpec extends AnyFlatSpec
  with Matchers with SharedSparkSql {

  override def conf: SparkConf = {
    val sparkWarehouseDir = fullPath(
      "src/test/resources/spark-warehouse")

    val testConfigPath = fullPath(
      "src/test/resources/application-test.conf")

    // override the location of the config to our testing config
    sys.props += ( ("config.file", testConfigPath ))

    SparkEventExtractorApp.sparkConf
      .setMaster("local[*]")
      .set("spark.app.id", appID)
      .set("spark.sql.warehouse.dir", sparkWarehouseDir)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestHelper.bootstrapTests(SparkEventExtractorApp.sparkSession)
  }

  "SparkEventExtractor" should " load and test customers " in {
    val testSession = SparkEventExtractorApp.sparkSession
    import testSession.implicits._

    // customers.json
    val customersDF = TestHelper.customersDataFrame(testSession)

    val customerRow = customersDF
      .where($"customerId".equalTo("CUST124"))
      .select($"nickname",$"membership")

    // test row level equality
    customerRow.head() shouldEqual Row("milocoffee", null)
  }

  "SparkEventExtractor" should
    "join customerRating events with customers" in {
    val testSession = SparkEventExtractorApp.sparkSession
    import testSession.implicits._

    val ratingEvents = TestHelper.customerRatingsDataFrame(testSession)

    SparkEventExtractor(testSession)
      .transform(ratingEvents)
      .select(
        $"firstName",
        $"lastName",
        $"rating").head shouldEqual Row("Milo", "Haines", 4)
  }

  "SparkEventExtractor" should
    "fails to join customerRating events when customer table is missing" in {
    val testSession = SparkEventExtractorApp.sparkSession
    testSession.sql("drop table customers")

    val ratingEvents = TestHelper.customerRatingsDataFrame(testSession)

    assertThrows[RuntimeException](
      SparkEventExtractor(testSession)
        .transform(ratingEvents))
  }

  "SparkEventExtractor" should
    "handle end to end processing" in {
    val testSession = SparkEventExtractorApp.sparkSession
    TestHelper.customersTable(testSession)
    TestHelper.customerRatingsTable(testSession)
    val destinationTable = "silver.customerRatings"
    testSession.conf.set(SourceTableName, "bronze.customerRatings")
    testSession.conf.set(DestinationTableName, destinationTable)
    testSession.conf.set(SaveModeName, "Overwrite")

    testSession.sql(s"drop table if exists $destinationTable")
    // reads from bronze db table -> joins with customer table -> writes joined table to silver db table
    SparkEventExtractorApp.run()

    testSession.catalog
      .tableExists(destinationTable) shouldBe true
    testSession.table(destinationTable).count shouldEqual 1
  }

  "SparkEventExtractor" should " read and join Customer EventData " in {
    val testSession = SparkEventExtractorApp.sparkSession
    import testSession.implicits._

    val customerDataset = testSession.createDataset(TestHelper.customers())
    customerDataset.printSchema()
    /*
    root
      |-- active: boolean (nullable = false)
      |-- created: timestamp (nullable = true)
      |-- customerId: string (nullable = true)
      |-- firstName: string (nullable = true)
      |-- lastName: string (nullable = true)
      |-- email: string (nullable = true)
      |-- nickname: string (nullable = true)
      |-- membership: struct (nullable = true)
      |    |-- customerId: string (nullable = true)
      |    |-- membershipId: string (nullable = true)
      |    |-- since: timestamp (nullable = true)
      |    |-- points: integer (nullable = false)
      |-- preferences: struct (nullable = true)
      |    |-- customerId: string (nullable = true)
      |    |-- preferencesId: string (nullable = true)
     */

    customerDataset.show(10, 0, vertical = true)

    // write as json or parquet or csv (can be used to build your own test data)
    // this was run once to enable you to have data to work with
    customerDataset.toJSON
      .coalesce(1)
      .write
      .mode(SaveMode.Ignore)
      .json(TestHelper.customerJsonPath)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    // add anything else you may need to clean up or track after the suite is complete
  }


}
