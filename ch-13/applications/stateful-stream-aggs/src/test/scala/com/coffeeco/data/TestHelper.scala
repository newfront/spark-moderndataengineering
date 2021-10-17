package com.coffeeco.data

import com.coffeeco.data.format.CoffeeOrder

import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.MINUTES
import java.time.{Instant, ZoneOffset, ZonedDateTime}
import scala.concurrent.duration.Duration

object TestHelper {

  // add common helpers methods here.

  def fullPath(filePath: String): String = new java.io.File(filePath).getAbsolutePath

  // 6:00 am on the 25th of September 2021
  val firstOrder: Instant = ZonedDateTime.of(2021,9,25,6,0,0,0, ZoneOffset.UTC).toInstant

  def coffeeOrderData(): Seq[CoffeeOrder] = {
    Seq(
      CoffeeOrder(firstOrder.toEpochMilli, "order1", "storeA", "custA", 2, 9.99f),
      CoffeeOrder(firstOrder.plus(1, MINUTES).toEpochMilli, "order2", "storeA", "custB", 1, 4.99f),
      CoffeeOrder(firstOrder.plus(2, MINUTES).toEpochMilli, "order3", "storeA", "custC", 3, 14.99f),
      CoffeeOrder(firstOrder.plus(3, MINUTES).toEpochMilli, "order5", "storeA", "custE", 2, 6.99f),
      CoffeeOrder(firstOrder.plus(3, MINUTES).toEpochMilli, "order4", "storeB", "custD", 1, 3.99f),
      CoffeeOrder(firstOrder.plus(4, MINUTES).toEpochMilli, "order7", "storeB", "custG", 2, 8.99f),
      // -- split 1
      CoffeeOrder(firstOrder.plus(4, MINUTES).toEpochMilli, "order8", "storeB", "custH", 3, 10.99f),
      CoffeeOrder(firstOrder.plus(4, MINUTES).toEpochMilli, "order6", "storeC", "custF", 1, 2.99f),
      CoffeeOrder(firstOrder.plus(4, MINUTES).toEpochMilli, "order6", "storeC", "custF", 1, 2.99f),
      CoffeeOrder(firstOrder.plus(5, MINUTES).toEpochMilli, "order9", "storeB", "custI", 1, 5.99f),
      CoffeeOrder(firstOrder.plus(6, MINUTES).toEpochMilli, "order10", "storeA", "custJ", 2, 7.99f),
      CoffeeOrder(firstOrder.plus(6, MINUTES).toEpochMilli, "order11", "storeC", "custK", 5, 27.56f),
      // -- split 2
      CoffeeOrder(firstOrder.plus(7, MINUTES).toEpochMilli, "order12", "storeA", "custL", 2, 1.99f),
      CoffeeOrder(firstOrder.plus(8, MINUTES).toEpochMilli, "order13", "storeB", "custA", 1, 3.99f),
      CoffeeOrder(firstOrder.plus(9, MINUTES).toEpochMilli, "order14", "storeC", "custB", 2, 7.99f),
      CoffeeOrder(firstOrder.plus(10, MINUTES).toEpochMilli, "order15", "storeB", "custD", 2, 3.99f),
      CoffeeOrder(firstOrder.plus(11, MINUTES).toEpochMilli, "order16", "storeC", "cust99", 1, 4.99f),
      CoffeeOrder(firstOrder.plus(12, MINUTES).toEpochMilli, "order17", "storeD", "cust100", 9, 39.99f),
      // -- split 3
      CoffeeOrder(firstOrder.plus(15, MINUTES).toEpochMilli, "order18", "storeA", "cust101", 1, 2.99f),
      // due to the window+watermark we will need to see data from the 21th minute
      CoffeeOrder(firstOrder.plus(16, MINUTES).toEpochMilli, "order19", "storeB", "cust102", 1, 4.99f),
      CoffeeOrder(firstOrder.plus(20, MINUTES).toEpochMilli, "order20", "storeF", "custbb", 3, 14.99f),
      // duplicate data (same as second item in the sequence) - can use dropDuplicates to ignore
      CoffeeOrder(firstOrder.plus(1, MINUTES).toEpochMilli, "order2", "storeA", "custB", 1, 4.99f),
      // this row has a timestamp from the first split point, with the window of 5 minutes, and watermark at 5 minutes
      // this row will be ignored, so the data for this third window will only have storeA and not storeE
      CoffeeOrder(firstOrder.plus(2, MINUTES).toEpochMilli, "order999", "storeE", "custB", 1, 4.99f),
      CoffeeOrder(firstOrder.plus(22, MINUTES).toEpochMilli, "orderN", "storeF", "custbbb", 9, 100.99f)
    )
  }
}
