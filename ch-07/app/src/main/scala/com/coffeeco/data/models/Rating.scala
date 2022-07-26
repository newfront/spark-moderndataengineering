package com.coffeeco.data.models

import java.sql.Timestamp

@SerialVersionUID(100L)
case class Rating(
  created: Timestamp,
  customerId: String,
  score: Int,
  item: Option[String],
  store: Option[String]
)
