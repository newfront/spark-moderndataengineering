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
    val progressSummary = queryProgress.prettyJson
    logger.info(s"query.progress stream.id=${queryProgress.id} stream.name=${queryProgress.name} " +
      s"\n progressSummary=$progressSummary")
  }

  override def onQueryTerminated(event: StreamingQueryListener.QueryTerminatedEvent): Unit = {
    if (event.exception.nonEmpty) {
      logger.error(s"query.terminated.with.exception exception=${event.exception.get}")
      // depending on the exception, you can tap into the terminated event
      // to trigger pager duty or collect the information about the stream that had
      // a failure
    } else {
      logger.info(s"query.terminated stream.id=${event.id} stream.run.id=${event.runId}")
    }
  }
}
