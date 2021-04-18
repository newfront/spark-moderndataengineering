package com.coffeeco.data.models

import java.sql.Timestamp

@SerialVersionUID(100L)
case class Item(
  itemId: String,
  availability: Availability,
  cost: Int,
  name: String,
  points: Int,
  vendor: String
)

@SerialVersionUID(100L)
case class Availability(
  now: Boolean,
  since: Timestamp,
  until: Timestamp,
  seasonal: Boolean
)

@SerialVersionUID(100L)
case class Vendor(
  active: Boolean,
  name: String,
  location: Location,
  vendorId: String
)
