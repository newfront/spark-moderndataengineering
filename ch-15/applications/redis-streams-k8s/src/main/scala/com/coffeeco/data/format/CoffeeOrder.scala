package com.coffeeco.data.format

@SerialVersionUID(101L)
case class CoffeeOrder(
  timestamp: Long,
  orderId: String,
  storeId: String,
  customerId: String,
  numItems: Int,
  price: Float
) extends Serializable

@SerialVersionUID(101L)
case class CoffeeOrderForAnalysis(
  timestamp: java.sql.Timestamp,
  window: Window,
  key: String,
  orderId: String,
  storeId: String,
  customerId: String,
  numItems: Int,
  price: Float
) extends Serializable

@SerialVersionUID(101L)
case class CoffeeOrderStats(
  storeId: String,
  window: Window,
  totalOrders: Int,
  totalItems: Int,
  totalRevenue: Float,
  averageNumItems: Float
) extends Serializable

@SerialVersionUID(101L)
case class Window(
  start: java.sql.Timestamp,
  end: java.sql.Timestamp
) extends Serializable
