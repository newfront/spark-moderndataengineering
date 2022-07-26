package com.coffeeco.data.models

import java.sql.Timestamp

@SerialVersionUID(100L)
case class Store(
  alias: String,
  storeId: String,
  capacity: Int,
  openSince: Timestamp,
  openNow: Boolean,
  opensAt: Int,
  closesAt: Int,
  storeInfo: StoreInfo,
  location: Location
)

@SerialVersionUID(100L)
case class StoreInfo(
  hasDriveThrough: Boolean,
  petFriendly: Boolean,
  outdoorSeating: Boolean
)

@SerialVersionUID(100L)
case class Location(
  locationId: String,
  city: String,
  state: String,
  country: String,
  timezone: String,
  longitude: String,
  latitude: String
)

@SerialVersionUID(100L)
case class Menu(
  menuId: String
  // will store products
)
