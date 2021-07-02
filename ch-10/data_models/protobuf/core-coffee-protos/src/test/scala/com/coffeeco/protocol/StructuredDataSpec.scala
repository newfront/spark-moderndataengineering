package com.coffeeco.protocol

import com.coffeeco.protocol.coffee.{Availability, Coffee, Vendor}
import com.coffeeco.protocol.coffee.Coffee.RoastLevel
import org.scalatest.{FlatSpec, Matchers}

import java.io.PrintWriter
import java.nio.file.{Files, Paths}

class StructuredDataSpec extends FlatSpec with Matchers {
  import java.time._
  import java.time.temporal._
  import scalapb.json4s.JsonFormat

  val ReleaseVersion = "1.0.0"

  val startingDate: Instant = Instant.parse("2021-05-01T00:00:00.00Z")
  val endingDate: Instant = startingDate.plus(30, ChronoUnit.DAYS)

  val drink: Coffee = Coffee(
    coffeeId="cf123",
    name="Grand Voyage",
    vendor = Some(Vendor(
      vendorId = "vn123",
      name = "Verve"
    )),
    boldness=RoastLevel.light,
    availability = Some(
      Availability(
        now = true,
        since = startingDate.toEpochMilli,
        until = endingDate.toEpochMilli,
        seasonal = true
      )
    )
  )

  "Coffee" should " can have a vendor and availability defined " in {
    drink.name shouldEqual "Grand Voyage"
    drink.vendor.isDefined shouldBe true
    drink.vendor.get.name shouldEqual "Verve"
    drink.availability.get.now shouldBe true
  }

  val versionedFileLocation: String = s"src/test/resources/coffee/$ReleaseVersion"

  "Coffee" should " be serializable to JSON" in {
    val coffeeAsJson = JsonFormat.toJsonString(drink)
    val outputFile = new java.io.File(
      s"$versionedFileLocation/coffee.json"
    )
    // for each version (ReleaseVersion) create a copy
    if (!outputFile.exists()) {
      val writer = new PrintWriter(outputFile)
      writer.write(coffeeAsJson)
      writer.close()
    }
    // stored the json representation for the version
    outputFile.exists() shouldBe true
  }

  "Coffee" should " be serializable as simple byte array" in {
    val bytes = drink.toByteArray
    val outputFile = new java.io.File(
      s"$versionedFileLocation/coffee.bin")
    // for each release version
    if (!outputFile.exists())
      Files.write(outputFile.toPath, bytes)
    // stored the binary representation for the version
    outputFile.exists() shouldBe true
  }

  "Coffee" should " be deserializable from JSON" in {
    val coffeeJson = Files.readString(
      Paths.get(s"$versionedFileLocation/coffee.json"))

    val drinkFromJson = JsonFormat.fromJsonString[Coffee](coffeeJson)
    drinkFromJson.vendor.get.name shouldEqual "Verve"
    drinkFromJson.availability.get.now shouldBe true
  }

  "Coffee" should " be deserializable from proto binary" in {
    val drinkFromBytes = Coffee.parseFrom(
      Files.readAllBytes(
        Paths.get(s"$versionedFileLocation/coffee.bin")))
    drinkFromBytes.vendor.get.name shouldEqual "Verve"
    drinkFromBytes.availability.get.now shouldBe true
  }
}
