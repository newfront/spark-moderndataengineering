package com.coffeeco.data.listeners

import org.apache.log4j.Logger
import org.apache.spark.TaskEndReason
import org.apache.spark.executor.{ExecutorMetrics, TaskMetrics}
import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd, SparkListenerApplicationStart, SparkListenerEnvironmentUpdate, SparkListenerEvent, SparkListenerExecutorAdded, SparkListenerExecutorExcluded, SparkListenerExecutorMetricsUpdate, SparkListenerExecutorRemoved, SparkListenerExecutorUnexcluded, SparkListenerJobEnd, SparkListenerJobStart, SparkListenerStageCompleted, SparkListenerStageExecutorMetrics, SparkListenerStageSubmitted, SparkListenerTaskEnd, SparkListenerTaskGettingResult, SparkListenerTaskStart, StageInfo, TaskInfo}
import org.apache.spark.sql.SparkSession

import java.util.Properties

case class SparkApplicationListener() extends SparkListener {
  val logger: Logger = Logger.getLogger(classOf[SparkApplicationListener])
  lazy val session: SparkSession = SparkSession.getDefaultSession.getOrElse {
    throw new RuntimeException("There is no SparkSession")
  }

  override def onApplicationStart(applicationStart: SparkListenerApplicationStart): Unit = {
    super.onApplicationStart(applicationStart)
    logger.info(s"app.start app.name=${applicationStart.appName}")
  }

  override def onJobStart(jobStart: SparkListenerJobStart): Unit = {
    super.onJobStart(jobStart)
    //val jobProps: Properties = jobStart.properties
    logger.info(s"job.start jobId=${jobStart.jobId} jobStart.time=${jobStart.time}")
  }

  override def onStageSubmitted(stageSubmitted: SparkListenerStageSubmitted): Unit = {
    super.onStageSubmitted(stageSubmitted)
    val stageInfo: StageInfo = stageSubmitted.stageInfo
    //val stageProperties: Properties = stageSubmitted.properties
    logger.info(s"stage.submitted stage.id=${stageInfo.stageId}")
  }

  override def onStageExecutorMetrics(executorMetrics: SparkListenerStageExecutorMetrics): Unit = {
    super.onStageExecutorMetrics(executorMetrics)
    val em: SparkListenerStageExecutorMetrics = executorMetrics
    logger.info(s"stage.executor.metrics stageId=${em.stageId} stage.attempt.id=${em.stageAttemptId} exec.id=${em.execId}")
  }

  override def onTaskStart(taskStart: SparkListenerTaskStart): Unit = {
    super.onTaskStart(taskStart)
    val ts = taskStart
    val taskInfo: TaskInfo = ts.taskInfo
    // taskInfo has a wealth of status and timing details
    logger.info(s"task.start " +
      s"task.id=${taskInfo.taskId} task.status=${taskInfo.status} task.attempt.number=${taskInfo.attemptNumber} " +
      s"stage.id=${ts.stageId} stage.attempt.id=${ts.stageAttemptId}"
    )
  }

  override def onTaskGettingResult(taskGettingResult: SparkListenerTaskGettingResult): Unit = {
    super.onTaskGettingResult(taskGettingResult)
    logger.info(s"task.getting.result ${taskGettingResult.taskInfo.taskId}")
  }

  override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
    super.onTaskEnd(taskEnd)
    val te = taskEnd
    val taskInfo: TaskInfo = te.taskInfo
    val taskEndReason: TaskEndReason = te.reason
    //val taskExecutorMetrics: ExecutorMetrics = te.taskExecutorMetrics
    val taskMetrics: TaskMetrics = te.taskMetrics
    /*
    taskMetrics.inputMetrics
    taskMetrics.outputMetrics
    taskMetrics.shuffleReadMetrics
    taskMetrics.shuffleWriteMetrics
    taskMetrics.diskBytesSpilled
    taskMetrics.memoryBytesSpilled
    taskMetrics.peakExecutionMemory
    taskMetrics.resultSize
    taskMetrics.resultSerializationTime
    taskMetrics.executorRunTime
    taskMetrics.executorCpuTime
    taskMetrics.executorDeserializeCpuTime
    taskMetrics.executorDeserializeTime
    */

    logger.info(s"task.end task.type=${taskEnd.taskType} end.reason=${taskEndReason.toString} task.id=${taskInfo.taskId}" +
      s"executor.cpu.time=${taskMetrics.executorCpuTime}")
  }

  override def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit = {
    super.onStageCompleted(stageCompleted)
    val stc = stageCompleted
    val stageInfo: StageInfo = stc.stageInfo
    //val stm: TaskMetrics = stageInfo.taskMetrics
    logger.info(s"stage.completed stage.id=${stageInfo.stageId} stage.completion.time=${stageInfo.completionTime.getOrElse(0)}")
  }

  override def onJobEnd(jobEnd: SparkListenerJobEnd): Unit = {
    super.onJobEnd(jobEnd)
    logger.info(s"job.end job.id=${jobEnd.jobId} end.time=${jobEnd.time}")
  }

  override def onExecutorAdded(executorAdded: SparkListenerExecutorAdded): Unit = {
    super.onExecutorAdded(executorAdded)
  }

  override def onExecutorExcluded(executorExcluded: SparkListenerExecutorExcluded): Unit = {
    super.onExecutorExcluded(executorExcluded)
  }

  override def onExecutorMetricsUpdate(executorMetricsUpdate: SparkListenerExecutorMetricsUpdate): Unit = {
    super.onExecutorMetricsUpdate(executorMetricsUpdate)
  }

  override def onExecutorUnexcluded(executorUnexcluded: SparkListenerExecutorUnexcluded): Unit = {
    super.onExecutorUnexcluded(executorUnexcluded)
  }

  override def onExecutorRemoved(executorRemoved: SparkListenerExecutorRemoved): Unit = {
    super.onExecutorRemoved(executorRemoved)
  }

  override def onEnvironmentUpdate(environmentUpdate: SparkListenerEnvironmentUpdate): Unit = {
    super.onEnvironmentUpdate(environmentUpdate)
  }

  override def onOtherEvent(event: SparkListenerEvent): Unit = {
    super.onOtherEvent(event)
  }

  override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {
    super.onApplicationEnd(applicationEnd)
    logger.info(s"application.ended end.time=${applicationEnd.time}")
  }

}
