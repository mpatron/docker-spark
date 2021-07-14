# Spark Listener Demo

This demonstrates Spark Job, Stage and Tasks Listeners

**1) Start spark-shell**

```bash
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 2.3.2.3.1.4.0-315
      /_/

Using Scala version 2.11.12 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_112)
Type in expressions to have them evaluated.
Type :help for more information.
```

**2) On Spark shell type** `:paste` **and copy paste below code snippet** 

```scala
/************** Listener Demo **************/
    import org.joda.time.format.DateTimeFormat

    import org.apache.spark.SparkContext
    import org.apache.spark.scheduler.{SparkListener, SparkListenerJobEnd, SparkListenerJobStart,
      SparkListenerStageCompleted, SparkListenerTaskEnd, SparkListenerStageSubmitted, SparkListenerTaskStart}
    val sparkContext = spark.sparkContext
    val sparkSession = spark
    val sqlContext = sparkSession.sqlContext
    object DateUtils {
      def timeToStr(epochMillis: Long): String =
        DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").print(epochMillis)
    }

    //Job Listener Example
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
    trait JobEventConsumer {
       def onJobStart(jobStart: SparkListenerJobStart)
       def onJobEnd(jobEnd: SparkListenerJobEnd)
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
    trait StageEventConsumer {
      def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit
      def onStageSubmitted(stageSubmitted: SparkListenerStageSubmitted): Unit
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
```
Note: Press Ctrl + D to come back to spark shell prompt

**3) Run a spark job  in the shell**

Note: You may copy paste below example code for testing
```scala
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
```

### Output
```bash
------->Job-0 started
job Start time: 2020-08-22 11:23:04
<----------------
------->Stage-0 submitted
stage name: $anonfun$withThreadLocalCaptured$1 at FutureTask.java:266
<----------------
------->Task-0  of Stage-0 Started-------->
Id: 0
Executor Id: 1
Host: 192.168.0.102
LaunchTime: 2020-08-22 11:23:04
<----------------
------->Task-1  of Stage-0 Started-------->
Id: 1
Executor Id: 2
Host: 192.168.0.102
LaunchTime: 2020-08-22 11:23:04
<----------------
------->Task-0  of Stage-0 Completed-------->                       (0 + 2) / 2]
Id: 0
TaskType: ResultTask
Executor Id: 1
Host: 192.168.0.102
Finish Time: 2020-08-22 11:23:06
Reason: Success
Records Written=0
Records Read=0
Executor RunTime=63
Executor Cpu Time=52342000
PeakExecutionMemory: 0
<----------------
------->Task-1  of Stage-0 Completed-------->                       (1 + 1) / 2]
Id: 1
TaskType: ResultTask
Executor Id: 2
Host: 192.168.0.102
Finish Time: 2020-08-22 11:23:07
Reason: Success
Records Written=0
Records Read=0
Executor RunTime=63
Executor Cpu Time=50597000
PeakExecutionMemory: 0
<----------------
------->Stage-0 completed
stage name: $anonfun$withThreadLocalCaptured$1 at FutureTask.java:266
Tasks count: 2
executorRunTime=126
executorCPUTime=102939000
<----------------
------->Job-0 Finished
job End time: 2020-08-22 11:23:07
Job Result: JobSucceeded
<----------------
------->Job-1 started
job Start time: 2020-08-22 11:23:07
<----------------
------->Stage-1 submitted
stage name: show at <console>:33
<----------------
------->Task-0  of Stage-1 Started-------->
Id: 2
Executor Id: 2
Host: 192.168.0.102
LaunchTime: 2020-08-22 11:23:07
<----------------
------->Task-0  of Stage-1 Completed-------->
Id: 2
TaskType: ResultTask
Executor Id: 2
Host: 192.168.0.102
Finish Time: 2020-08-22 11:23:08
Reason: Success
Records Written=0
Records Read=0
Executor RunTime=433
Executor Cpu Time=397209000
PeakExecutionMemory: 16778240
<----------------
------->Stage-1 completed
stage name: show at <console>:33
Tasks count: 1
executorRunTime=433
executorCPUTime=397209000
<----------------
------->Job-1 Finished
job End time: 2020-08-22 11:23:08
Job Result: JobSucceeded
<----------------
------->Job-2 started
job Start time: 2020-08-22 11:23:08
<----------------
------->Stage-2 submitted
stage name: show at <console>:33
<----------------
------->Task-0  of Stage-2 Started-------->
Id: 3
Executor Id: 2
Host: 192.168.0.102
LaunchTime: 2020-08-22 11:23:08
<----------------
------->Task-0  of Stage-2 Completed-------->
Id: 3
TaskType: ResultTask
Executor Id: 2
Host: 192.168.0.102
Finish Time: 2020-08-22 11:23:08
Reason: Success
Records Written=0
Records Read=0
Executor RunTime=2
Executor Cpu Time=2463000
PeakExecutionMemory: 16778240
<----------------
------->Stage-2 completed
stage name: show at <console>:33
Tasks count: 1
executorRunTime=2
executorCPUTime=2463000
<----------------
------->Job-2 Finished
job End time: 2020-08-22 11:23:08
Job Result: JobSucceeded
<----------------
+----------+---------+---------+--------+----------+
|first_name|     city|     city| country|population|
+----------+---------+---------+--------+----------+
|    andrea| medellin| medellin|colombia|       2.5|
|   rodolfo| medellin| medellin|colombia|       2.5|
|     abdul|bangalore|bangalore|   india|      12.3|
+----------+---------+---------+--------+----------+
```
Try to understand the output of Job, Stage and Task Listener. With this example, you may find Number of records processed, CPU usage, Peak memory usage/tasks etc

**4) To unregister the event subscrriptions**

```scala
  /************** Remove Listeners **************/
    sparkContext.removeSparkListener(jm)
    sparkContext.removeSparkListener(sm)
    sparkContext.removeSparkListener(rm)
    /**************                 **************/
```
