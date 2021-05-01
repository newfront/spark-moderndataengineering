package com.coffeeco.data

import com.coffeeco.data.models.{Customer, CustomerRatingEventType, Membership, Preferences}
import org.apache.spark.sql.{DataFrame, Encoder, Encoders, SaveMode, SparkSession}

import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

object TestHelper {

  // add common helpers methods here.

  def fullPath(filePath: String): String = new java.io.File(filePath).getAbsolutePath

  val customerJsonPath: String = "src/test/resources/customers/customers.json"
  val customerRatingsPath: String = "src/test/resources/customers/customer_ratings.json"
  val customerEncoder: Encoder[Customer] = Encoders.product[Customer]

  def customers(): Seq[Customer] = {
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
    Seq(customer, customer2)
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
        .mode(SaveMode.Overwrite)
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

  def writeCustomerRatingsJson(spark: SparkSession): Unit = {
    val customersRatingsJson: String = new java.io.File(
      customerRatingsPath
    ).getAbsolutePath

    customerRatingsDataFrame(spark)
      .coalesce(1)
      .write
      .mode(SaveMode.Ignore)
      .json(customersRatingsJson)
  }

  def customerRatingsTable(spark: SparkSession): Unit = {
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

    customersTable(spark)
    customerRatingsTable(spark)
  }

}
