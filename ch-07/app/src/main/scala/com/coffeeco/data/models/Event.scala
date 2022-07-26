package com.coffeeco.data.models

import java.sql.Timestamp

sealed trait Event

trait BasicEvent extends Event {
  def created: Timestamp
  def eventType: String
  def label: String
}

trait CustomerEvent extends BasicEvent {
  def customerId: String
}

trait RatingEvent extends BasicEvent {
  def rating: Int
  def ratingType: String
  def storeId: Option[String] = None
  def itemId: Option[String] = None
}

@SerialVersionUID(100L)
case class BasicEventType(
  override val created: Timestamp,
  override val eventType: String = "BasicEvent",
  override val label: String) extends BasicEvent

@SerialVersionUID(100L)
case class CustomerEventType(
  override val created: Timestamp,
  override val eventType: String = "CustomerEvent",
  override val label: String,
  customerId: String
) extends CustomerEvent

@SerialVersionUID(100L)
case class RatingEventType(
  override val created: Timestamp,
  override val eventType: String = "RatingEvent",
  override val label: String,
  rating: Int, // 1-5
  ratingType: String // rating.store or rating.item
) extends RatingEvent

@SerialVersionUID(100L)
case class CustomerRatingEventType(
  override val created: Timestamp,
  override val eventType: String = "CustomerRatingEventType",
  override val label: String,
  override val customerId: String,
  override val rating: Int,
  override val ratingType: String,
  override val storeId: Option[String] = None,
  override val itemId: Option[String] = None
) extends CustomerEvent with RatingEvent



