package com.coffeeco.data.listeners

import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{StreamingQueryListener, StreamingQueryProgress}

case class QueryListener() extends StreamingQueryListener {
  val logger: Logger = Logger.getLogger(classOf[QueryListener])
  val session: SparkSession = SparkSession.getDefaultSession.getOrElse {
    throw new RuntimeException("There is no SparkSession")
  }

  override def onQueryStarted(event: StreamingQueryListener.QueryStartedEvent): Unit = {
    logger.info(s"query.started stream.id=${event.id} stream.name=${event.name} " +
      s"stream.run.id=${event.runId} stream.start.time=${event.timestamp}")
  }

  override def onQueryProgress(event: StreamingQueryListener.QueryProgressEvent): Unit = {
    val queryProgress: StreamingQueryProgress = event.progress
    val batchId = queryProgress.batchId

    val batchDuration = queryProgress.batchDuration
    val inputRowsRate = queryProgress.inputRowsPerSecond
    val processedRowsRate = queryProgress.processedRowsPerSecond
    val outputRowsPerBatch = queryProgress.sink.numOutputRows

    val progressSummary = queryProgress.prettyJson
    logger.info(s"query.progress stream.id=${queryProgress.id} stream.name=${queryProgress.name} " +
      s" batch.id=$batchId batch.duration=$batchDuration" +
      s" rate: input.rows.per.second=$inputRowsRate processed.rows.per.second=$processedRowsRate " +
      s" sink.output.rows.per.batch=$outputRowsPerBatch " +
      s"\n progressSummary=$progressSummary")
  }

  override def onQueryTerminated(event: StreamingQueryListener.QueryTerminatedEvent): Unit = {
    if (event.exception.nonEmpty) {
      logger.error(s"query.terminated.with.exception exception=${event.exception.get}")
      // depending on the exception, you can tap into the terminated event
      // to trigger pager duty or collect the information about the stream that had
      // a failure

      // Options:
      // 1. Reset any terminated stream
      // resetTerminated()

      // 2. Shutdown the entire application
      // shutdown()

      // 3. Be creative and have fun, there are many ways to solve problems
    } else {
      logger.info(s"query.terminated stream.id=${event.id} stream.run.id=${event.runId}")
    }
  }

  def resetTerminated(): Unit = {
    session.streams.resetTerminated()
  }

  def haltAllStreams(): Unit = {
    session.streams.active.foreach { query =>
      logger.info(s"stream.stop name=${query.name} stream.id=${query.id}")
      query.stop()
    }
  }

  def shutdown(): Unit = {
    haltAllStreams()
    session.sparkContext.cancelAllJobs()
    session.stop()
  }
}