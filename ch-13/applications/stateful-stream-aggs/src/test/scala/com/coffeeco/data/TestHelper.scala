package com.coffeeco.data

import com.coffeeco.data.format.CoffeeOrder

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import scala.concurrent.duration.Duration

object TestHelper {

  // add common helpers methods here.

  def fullPath(filePath: String): String = new java.io.File(filePath).getAbsolutePath

  // 6:00 am on the 25th of September 2021
  val firstOrder: Instant = ZonedDateTime.of(2021,9,25,6,0,0,0, ZoneOffset.UTC).toInstant
  val tenMinutes: Long = Duration("10 minutes").toSeconds

  def coffeeOrderData(): Seq[CoffeeOrder] = {
    Seq(
      CoffeeOrder(firstOrder.toEpochMilli, "order1", "storeA", "custA", 2, 9.99f),
      CoffeeOrder(firstOrder.plusSeconds(60).toEpochMilli, "order2", "storeA", "custB", 1, 4.99f),
      CoffeeOrder(firstOrder.plusSeconds(60*2).toEpochMilli, "order3", "storeA", "custC", 3, 14.99f),
      CoffeeOrder(firstOrder.plusSeconds(60*2+10).toEpochMilli, "order4", "storeB", "custD", 1, 3.99f),
      CoffeeOrder(firstOrder.plusSeconds(60*2+20).toEpochMilli, "order5", "storeA", "custE", 2, 6.99f),
      CoffeeOrder(firstOrder.plusSeconds(60*2+30).toEpochMilli, "order6", "storeC", "custF", 1, 2.99f),

      CoffeeOrder(firstOrder.plusSeconds(60*2+40).toEpochMilli, "order6", "storeC", "custF", 1, 2.99f),
      CoffeeOrder(firstOrder.plusSeconds(60*2+50).toEpochMilli, "order7", "storeB", "custG", 2, 8.99f),
      CoffeeOrder(firstOrder.plusSeconds(60*3).toEpochMilli, "order8", "storeB", "custH", 3, 10.99f),
      CoffeeOrder(firstOrder.plusSeconds(60*4).toEpochMilli, "order9", "storeB", "custI", 1, 5.99f),
      // split first five minutes
      CoffeeOrder(firstOrder.plusSeconds(60*5).toEpochMilli, "order10", "storeA", "custJ", 2, 7.99f),
      CoffeeOrder(firstOrder.plusSeconds(60*5+30).toEpochMilli, "order11", "storeC", "custK", 5, 27.56f),

      CoffeeOrder(firstOrder.plusSeconds(60*6+30).toEpochMilli, "order12", "storeA", "custL", 2, 1.99f),
      CoffeeOrder(firstOrder.plusSeconds(60*7).toEpochMilli, "order13", "storeB", "custA", 1, 3.99f),
      CoffeeOrder(firstOrder.plusSeconds(60*8).toEpochMilli, "order14", "storeC", "custB", 2, 7.99f),
      // split second five minutes
      CoffeeOrder(firstOrder.plusSeconds(tenMinutes).toEpochMilli, "order15", "storeB", "custD", 2, 3.99f),
      CoffeeOrder(firstOrder.plusSeconds(tenMinutes+60).toEpochMilli, "order16", "storeC", "cust99", 1, 4.99f),
      CoffeeOrder(firstOrder.plusSeconds(tenMinutes+60*2).toEpochMilli, "order17", "storeD", "cust100", 9, 39.99f),

      // third five minute mark
      CoffeeOrder(firstOrder.plusSeconds(tenMinutes+60*5).toEpochMilli, "order18", "storeA", "cust101", 1, 2.99f),
      // due to the window+watermark we will need to see data from the 21th minute
      CoffeeOrder(firstOrder.plusSeconds(tenMinutes+60*6).toEpochMilli, "order19", "storeB", "cust102", 1, 4.99f),
      CoffeeOrder(firstOrder.plusSeconds(tenMinutes+2).toEpochMilli, "order20", "storeF", "custbb", 3, 14.99f),
      // duplicate data (same as second item in the sequence) - can use dropDuplicates to ignore
      CoffeeOrder(firstOrder.plusSeconds(60).toEpochMilli, "order2", "storeA", "custB", 1, 4.99f),
      // this row has a timestamp from the first split point, with the window of 5 minutes, and watermark at 5 minutes
      // this row will be ignored, so the data for this third window will only have storeA and not storeE
      CoffeeOrder(firstOrder.plusSeconds(60).toEpochMilli, "order999", "storeE", "custB", 1, 4.99f),
      CoffeeOrder(firstOrder.plusSeconds(tenMinutes*2+60).toEpochMilli, "orderN", "storeF", "custbbb", 9, 100.99f)
    )
  }
}
