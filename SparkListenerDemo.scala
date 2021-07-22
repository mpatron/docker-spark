import org.joda.time.format.DateTimeFormat
import org.apache.spark.SparkContext
import org.apache.spark.scheduler.{SparkListener, SparkListenerJobEnd, SparkListenerJobStart, SparkListenerStageCompleted, SparkListenerTaskEnd, SparkListenerStageSubmitted, SparkListenerTaskStart}

import scala.http.{Http, HttpOptions}


class LogManager {
  def log( a:String, b:String ) : String = {
      val data = (new StringBuilder()).append("\"{\id:\"").append(a).append("\",\"message:\"").append(b).append("\"}\"").toString()
      val result = Http("http://fluentd:9880/app.log").postData(data)
        .header("Content-Type", "application/json")
        .header("Charset", "UTF-8")
        .option(HttpOptions.readTimeout(10000)).asString
      return result
   }
}



val sparkContext = spark.sparkContext
val sparkSession = spark
val sqlContext = sparkSession.sqlContext
object DateUtils {
  def timeToStr(epochMillis: Long): String =
    DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").print(epochMillis)
}
//Job Listener Example

trait JobEventConsumer {
   def onJobStart(jobStart: SparkListenerJobStart)
   def onJobEnd(jobEnd: SparkListenerJobEnd)
}

/**************Job Listener Example **************/
class JobEventManager extends SparkListener {
  private val consumerMap: scala.collection.mutable.Map[String, JobEventConsumer] = scala.collection.mutable.Map[String, JobEventConsumer]()

  def addEventConsumer(SparkContext: SparkContext, id: String, consumer: JobEventConsumer) {
    consumerMap += (id -> consumer)
  }
  def removeEventConsumer(id: String) {
    consumerMap -= id
  }
  override def onJobStart(jobStart: SparkListenerJobStart): Unit = {
    consumerMap.foreach{ case (_,v ) =>
      if ( v != null ) {
        v.onJobStart(jobStart)
      }
    }
  }
  override def onJobEnd(jobEnd: SparkListenerJobEnd): Unit = {
    consumerMap.foreach{ case (_,v ) =>
      if ( v != null ) {
        v.onJobEnd(jobEnd)
      }
    }
  }
}

class JobEventConsumerImpl extends JobEventConsumer {
  override def onJobStart(jobStart: SparkListenerJobStart): Unit = {
    println (
      s"------->Job-${jobStart.jobId} started" +
        s"\njob Start time: ${DateUtils.timeToStr(jobStart.time)} " +
        s"\n<----------------")
  }
  override def onJobEnd(jobEnd: SparkListenerJobEnd): Unit = {
    println (
      s"------->Job-${jobEnd.jobId} Finished" +
        s"\njob End time: ${DateUtils.timeToStr(jobEnd.time)} " +
        s"\nJob Result: ${jobEnd.jobResult} " +
        s"\n<----------------")
  }
}
val jm = new JobEventManager
jm.addEventConsumer(sparkContext,"JEC1", new JobEventConsumerImpl)
//Register Task event listener
sparkContext.addSparkListener(jm)
/**************                        **************/

trait StageEventConsumer {
  def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit
  def onStageSubmitted(stageSubmitted: SparkListenerStageSubmitted): Unit
}

/**************Stage Listener Example **************/
class StageEventManager extends SparkListener {
  private val consumerMap: scala.collection.mutable.Map[String, StageEventConsumer] = scala.collection.mutable.Map[String, StageEventConsumer]()
  def addEventConsumer(SparkContext: SparkContext, id: String, consumer: StageEventConsumer) {
    consumerMap += (id -> consumer)
  }
  def removeEventConsumer(id: String) {
    consumerMap -= id
  }
  override def onStageSubmitted(stageSubmitted: SparkListenerStageSubmitted): Unit = {
    consumerMap.foreach{ case (_,v ) =>
      if ( v != null ) {
        v.onStageSubmitted(stageSubmitted)
      }
    }
  }
  override def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit = {
    consumerMap.foreach{ case (_,v ) =>
      if ( v != null ) {
        v.onStageCompleted(stageCompleted)
      }
    }
  }
}

class StageEventConsumerImpl extends StageEventConsumer {
  override def onStageCompleted(stageCompleted: SparkListenerStageCompleted){
    println (
      s"------->Stage-${stageCompleted.stageInfo.stageId} completed" +
      s"\nstage name: ${stageCompleted.stageInfo.name} " +
      s"\nTasks count: ${stageCompleted.stageInfo.numTasks} " +
      s"\nexecutorRunTime=${stageCompleted.stageInfo.taskMetrics.executorRunTime} " +
      s"\nexecutorCPUTime=${stageCompleted.stageInfo.taskMetrics.executorCpuTime} " +
      s"\n<----------------")
  }
  override def onStageSubmitted(stageSubmitted: SparkListenerStageSubmitted): Unit = {
    println (s"------->Stage-${stageSubmitted.stageInfo.stageId} submitted" +
      s"\nstage name: ${stageSubmitted.stageInfo.name} " +
      s"\n<----------------")
  }
}
val sm = new StageEventManager
sm.addEventConsumer(sparkContext,"SEC1", new StageEventConsumerImpl)
//Register stage event listener
sparkContext.addSparkListener(sm)
/**************                        **************/
/**************Task Listener Example **************/
class TaskEventManager extends SparkListener
{
  var consumerMap: scala.collection.mutable.Map[String, TaskEventConsumer] = scala.collection.mutable.Map[String, TaskEventConsumer]()
  def addEventConsumer(SparkContext: SparkContext, id: String, consumer: TaskEventConsumer)
  {
    consumerMap += (id -> consumer)
  }
  def removeEventConsumer(id: String)
  {
    consumerMap -= id
  }
  override def onTaskStart(taskStart: SparkListenerTaskStart): Unit = {
    for ( (_, v) <- consumerMap ) {
      if ( v != null ) {
        v.onTaskStart(taskStart)
      }
    }
  }
  override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit =  {
    for ( (_, v) <- consumerMap ) {
      if ( v != null ) {
        v.onTaskEnd(taskEnd)
      }
    }
  }
}
trait TaskEventConsumer {
  def onTaskStart(taskStart: SparkListenerTaskStart)
  def onTaskEnd(taskEnd: SparkListenerTaskEnd)
}
class TaskEventConsumerImpl extends TaskEventConsumer{
  def onTaskStart(taskStart: SparkListenerTaskStart): Unit = {
    println (
      s"------->Task-${taskStart.taskInfo.index}  of Stage-${taskStart.stageId} Started-------->" +
        s"\nId: ${taskStart.taskInfo.taskId} " +
        s"\nExecutor Id: ${taskStart.taskInfo.executorId} " +
        s"\nHost: ${taskStart.taskInfo.host} " +
        s"\nLaunchTime: ${DateUtils.timeToStr(taskStart.taskInfo.launchTime)} " +
        s"\n<----------------")
  }
  override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
    println (
      s"------->Task-${taskEnd.taskInfo.index}  of Stage-${taskEnd.stageId} Completed-------->" +
        s"\nId: ${taskEnd.taskInfo.taskId} " +
        s"\nTaskType: ${taskEnd.taskType} " +
        s"\nExecutor Id: ${taskEnd.taskInfo.executorId} " +
        s"\nHost: ${taskEnd.taskInfo.host} " +
        s"\nFinish Time: ${DateUtils.timeToStr(taskEnd.taskInfo.finishTime)} " +
        s"\nReason: ${taskEnd.reason} " +
        s"\nRecords Written=${taskEnd.taskMetrics.outputMetrics.recordsWritten} " +
        s"\nRecords Read=${taskEnd.taskMetrics.inputMetrics.recordsRead} " +
        s"\nExecutor RunTime=${taskEnd.taskMetrics.executorRunTime} " +
        s"\nExecutor Cpu Time=${taskEnd.taskMetrics.executorCpuTime} " +
        s"\nPeakExecutionMemory: ${taskEnd.taskMetrics.peakExecutionMemory} " +
        s"\n<----------------")
  }
}
val rm = new TaskEventManager
rm.addEventConsumer(sparkContext, "RLEC1", new TaskEventConsumerImpl)
//Register Task event listener
sparkContext.addSparkListener(rm)
/**************                        **************/

sc.setLogLevel("WARN")
val peopleDF = Seq(
  ("andrea", "medellin"),
  ("rodolfo", "medellin"),
  ("abdul", "bangalore")
).toDF("first_name", "city")

peopleDF.show()

val citiesDF = Seq(
  ("medellin", "colombia", 2.5),
  ("bangalore", "india", 12.3)
).toDF("city", "country", "population")

citiesDF.show()

peopleDF.join(
  citiesDF,
  peopleDF("city") <=> citiesDF("city")
).show()
