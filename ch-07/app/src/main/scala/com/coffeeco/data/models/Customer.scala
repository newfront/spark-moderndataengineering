package com.coffeeco.data.models

import java.sql.Timestamp

@SerialVersionUID(100L)
case class Customer(
  active: Boolean,
  created: Timestamp,
  customerId: String,
  firstName: String,
  lastName: String,
  email: String,
  nickname: String,
  membership: Option[Membership] = None,
  preferences: Option[Preferences] = None
)

@SerialVersionUID(100L)
case class Membership(
  customerId: String,
  membershipId: String,
  since: Timestamp,
  points: Int // customers collect points for purchases (rewards, etc)
)

@SerialVersionUID(100L)
case class Preferences(
  customerId: String,
  preferencesId: String
)
