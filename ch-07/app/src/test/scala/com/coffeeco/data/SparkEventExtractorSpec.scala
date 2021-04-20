package com.coffeeco.data

import com.coffeeco.data.models.{Customer, CustomerEventType, CustomerRatingEventType, Event, Membership, Preferences}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, Encoder, Encoders, Row, SaveMode, SparkSession}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.sql.Timestamp
import java.time._
import java.time.temporal.{ChronoUnit, TemporalUnit}

class SparkEventExtractorSpec extends AnyFlatSpec with SharedSparkSql with Matchers {

  override def conf: SparkConf = {
    val testConfigPath: String = new java.io.File(
      "src/test/resources/application-test.conf"
    ).getAbsolutePath

    val sparkWarehouseDir: String = new java.io.File(
      "src/test/resources/spark-warehouse"
    ).getAbsolutePath

    // override the location of the config to our testing config
    sys.props += ( ("config.file", testConfigPath ))

    SparkEventExtractorApp.sparkConf
      .setMaster("local[*]")
      .set("spark.app.id", appID)
      .set("spark.sql.warehouse.dir", sparkWarehouseDir)
  }

  val customerEncoder: Encoder[Customer] = Encoders.product[Customer]
  val customerJsonPath: String = "src/test/resources/customers/customers.json"

  "SparkEventExtractor" should " read and join Customer EventData " in {

    val testSession = SparkEventExtractorApp.sparkSession

    import testSession.implicits._

    val custJoin = Timestamp.from(Instant.ofEpochMilli(1618788493198L))
    val custId = "CUST123"

    val cust2Join = Timestamp.from(custJoin.toInstant.plus(4, ChronoUnit.DAYS))

    // generate the Customer Data Model
    val customer = Customer(
      active = true,
      created = custJoin,
      customerId = custId,
      firstName = "Scott",
      lastName  = "Haines",
      email = "scott@coffeeco.com",
      nickname = "scaines",
      membership = Some(
        Membership(
          customerId = custId,
          membershipId = "MEMB123",
          since = custJoin,
          points = 200
        )
      ),
      preferences = Some(
        Preferences(
          customerId = custId,
          preferencesId = "PREF123"
        )
      )
    )

    val customer2 = Customer(
      active = true,
      created = cust2Join,
      customerId = "CUST124",
      firstName = "Milo",
      lastName = "Haines",
      email = "milo@coffeeco.com",
      nickname = "milocoffee"
    )

    // convert to dataset via Encoder.product

    val customerDataset = testSession.createDataset(
      Seq(customer,customer2)
    )

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
    customerDataset.toJSON
      .coalesce(1)
      .write
      .mode(SaveMode.Overwrite)
      .json(customerJsonPath)
  }

  def customersDataFrame(spark: SparkSession): DataFrame = {
    val customersJson: String = new java.io.File(
      customerJsonPath
    ).getAbsolutePath

    spark.read
      .option("inferSchema", value = false)
      .schema(customerEncoder.schema)
      .json(customersJson)
  }

  def customersTable(spark: SparkSession): Unit = {
    if (!spark.catalog.tableExists("default", "customers")) {
      customersDataFrame(spark)
        .write
        .mode(SaveMode.Ignore)
        .saveAsTable("customers")
    }
  }

  def customerRatingsDataFrame(spark: SparkSession): DataFrame = {
    spark
      .createDataFrame[CustomerRatingEventType](Seq(
        CustomerRatingEventType(
          created = Timestamp.from(Instant.ofEpochMilli(1618788493198L)),
          label = "customer.rating",
          customerId = "CUST124",
          rating = 4,
          ratingType = "rating.store",
          storeId = Some("STOR123")
        )
      ))
  }

  def customerRatingsTable(spark: SparkSession): Unit = {
    bootstrapTests(spark)
    if (!spark.catalog.tableExists("bronze", "customerRatings")) {
      customerRatingsDataFrame(spark)
        .coalesce(1)
        .write
        .mode(SaveMode.Ignore)
        .saveAsTable("bronze.customerRatings")
    }
  }

  def bootstrapTests(spark: SparkSession): Unit = {
    val sqlWarehouse = spark.catalog.getDatabase("default").locationUri
    val bronzeDB = s"$sqlWarehouse/bronze"
    val silverDB = s"$sqlWarehouse/silver"

    spark.sql(
      s"""
        CREATE DATABASE IF NOT EXISTS bronze
        COMMENT 'raw source data'
        LOCATION '$bronzeDB'
        WITH
        DBPROPERTIES(TEAM='coffee-core',TEAM_SLACK='#coffeeco-eng-core');
        """)

    spark.sql(
      s"""
        CREATE DATABASE IF NOT EXISTS silver
        COMMENT 'reliable source data'
        LOCATION '$silverDB'
        WITH
        DBPROPERTIES(TEAM='coffee-core',TEAM_SLACK='#coffeeco-eng-core');
        """)
  }

  "SparkEventExtractor" should " load and test customers " in {
    val testSession = SparkEventExtractorApp.sparkSession
    import testSession.implicits._

    // customers.json
    val customersDF = customersDataFrame(testSession)

    val customerRow = customersDF
      .where($"customerId".equalTo("CUST124"))
      .select($"nickname",$"membership")

    // test row level equality
    customerRow.head() shouldEqual Row("milocoffee", null)
  }

  "SparkEventExtractor" should " extract and join customerRating events" in {

    val testSession = SparkEventExtractorApp.sparkSession
    import testSession.implicits._

    val ratingEvents = customerRatingsDataFrame(testSession)

    val extractor = SparkEventExtractor(testSession)
    // joined with customer table
    val combinedDf = extractor.process(ratingEvents)

    combinedDf
      .select($"firstName", $"lastName").head shouldEqual Row("Milo", "Haines")
  }

  "SparkEventExtractor" should " run full end to end processing" in {
    val testSession = SparkEventExtractorApp.sparkSession
    customerRatingsTable(testSession)
    import testSession.implicits._

    testSession.conf.set(SparkEventExtractorApp.Conf.SourceTableName, "bronze.customerRatings")
    testSession.conf.set(SparkEventExtractorApp.Conf.DestinationTableName, "silver.customerRatings")

    // reads from bronze db table -> joins with customer table -> writes joined table to silver db table
    SparkEventExtractorApp.run(SaveMode.Overwrite)

    testSession.catalog.tableExists("silver", "customerRatings") shouldBe true

  }

}
