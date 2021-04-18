package com.coffeeco.data

import com.coffeeco.data.models.{Customer, Membership, Preferences}
import org.apache.spark.SparkConf
import org.scalatest.flatspec.AnyFlatSpec

import java.sql.Timestamp
import java.time._

class SparkEventExtractorSpec extends AnyFlatSpec with SharedSparkSql {


  override def conf: SparkConf = {
    val testConfigPath: String = new java.io.File(
      "src/test/resources/application-test.conf"
    ).getAbsolutePath

    // override the location of the config to our testing config
    sys.props += ( ("config.file", testConfigPath ))

    SparkEventExtractorApp.sparkConf
      .setMaster("local[*]")
      .set("spark.app.id", appID)
  }

  "SparkEventExtractor" should " read and join Customer EventData " in {

    val testSession = SparkEventExtractorApp.sparkSession

    import testSession.implicits._

    val userJoined = Timestamp.from(Instant.ofEpochMilli(1618788493198L))
    val custId = "CUST123"

    val membership = Membership(
      customerId = custId,
      membershipId = "MEMB123",
      since = userJoined,
      points = 200
    )

    val preferences = Preferences(
      customerId = custId,
      preferencesId = "PREF123"
    )

    // generate the Customer Data Model
    val customer = Customer(
      active = true,
      created = userJoined,
      customerId = custId,
      firstName = "Scott",
      lastName  = "Haines",
      email = "scott@coffeeco.com",
      nickname = "scaines",
      membership = Some(membership),
      preferences = Some(preferences)
    )

    // convert to dataset via Encoder.product

    val customerDataset = testSession.createDataset(Seq(customer))

    customerDataset.show(10, 0, vertical = true)
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


  }

}
