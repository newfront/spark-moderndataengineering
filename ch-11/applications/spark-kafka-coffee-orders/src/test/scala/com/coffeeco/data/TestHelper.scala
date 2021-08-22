package com.coffeeco.data

object TestHelper {

  // add common helpers methods here.

  def fullPath(filePath: String): String = new java.io.File(filePath).getAbsolutePath

  object Kafka {

    case class MockKafkaDataFrame(key: Array[Byte], value: Array[Byte])

  }

}

