package com.coffeeco.data

import com.holdenkarau.spark.testing.{LocalSparkContext, SparkContextProvider}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, Suite}

object TestHelper {

}

trait SharedSparkSql extends BeforeAndAfterAll with SparkContextProvider {
  self: Suite =>

  @transient var _sparkSql: SparkSession = _
  @transient private var _sc: SparkContext = _

  override def sc: SparkContext = _sc

  def conf: SparkConf

  def sparkSql: SparkSession = _sparkSql

  override def beforeAll() {
    _sparkSql = SparkSession.builder().config(conf).getOrCreate()

    _sc = _sparkSql.sparkContext
    setup(_sc)
    super.beforeAll()
  }

  override def afterAll() {
    try {
      _sparkSql.close()
      _sparkSql = null
      LocalSparkContext.stop(_sc)
      _sc = null
    } finally {
      super.afterAll()
    }
  }

}