Sqsfork
======

High performance, Akka based Amazon SQS messages consumer for Scala.
Tested with scala 2.10 and Java 7

SQS is a complete message solution powered by Amazon, including monitoring, alarms, redundancy, etc. 
Due to its particularities, we decided to build a message consumer from scratch, to better handle costs, latency and others.

Getting Start
-------------

1.  Add sqsfork to your project:

  ```scala
  libraryDependencies += "sqsfork" %% "sqsfork" % "1.0"
  ```

2. Add a worker to process messages asynchronously:

  ```scala
    import com.sqsfork.SQSWorker
    import com.amazonaws.services.sqs.model.Message
  
    class HardWorker extends SQSWorker {

      def config = Map("queueName" -> "test", "concurrency" -> "10")
		  
      def perform(message: Message) {
        println(message.getBody())
      }

    }
  ```
  Configure the parallelism using the param **concurrency**, the default is 10. 	
  

3. Start the worker:

  ```scala
    import com.sqsfork.Credentials

    object Main extends App {
      
      implicit val credendials = Credentials("yourAwsAccessKey","yourAwsSecretAccessKey")
                                                   
      implicit val waitForever = true
  
      val worker = new HardWorker
      worker.run
      
    }
  ```
  
  With **waitForever = false**, the engine starts and this thread continues to execute. If **waitForever = true** the thread 
  will be blocked (suitable to be executed in different process, like heroku workers)

Deploy
------

You can call **worker.run** in any initializer application hook (in this case use **waitForever = false**)
If you want to run your worker in a different process, use a Procfile (like Heroku does) to start your workers. To generate a start script, see [sbt-start-script](https://github.com/sbt/sbt-start-script)


Tips and Limitations
--------------------

* Make sure to disable data caches (or at least clean the data after each execution) in your workers, otherwise your workers will explode.
* Workers will automatically retry a job if the execution raises an exception
* If the execution does not raise an exception, workers assume the execution ran cleanly and will remove the message from queue 
* Be aware of the costs of using SQS, even while under the service's free tier

### Future and TODO's

* Implement a better retry policy (today the message will be retried forever)
* Add more tests
* Delay new fetches if the queue is empty
