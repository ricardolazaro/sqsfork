package com.sqsfork

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._

import com.amazonaws.services.sqs.model.Message

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.routing.RoundRobinRouter
import akka.util.Timeout

case class SQSBatchDone(messages: List[Message])
case class SQSMessage(message: Message)
case class SQSMessages(messages: List[Message])
case class SQSFetchDone(messages: List[Message])
case class SQSProcessDone(message: Message, successfull: Boolean)

/**
 * This actor fetches messages from SQS queue
 * and send the messages to the manager actor
 */
class SQSFetchActor(sqsHelper: SQSHelper) extends Actor with ActorLogging {
  def receive = {
    case "fetch" => {
      log.info("fetching messages...")
      val messages = sqsHelper.fetchMessages
      sender ! SQSFetchDone(messages)
    }
  }
}

/**
 * This actor deletes messages from SQS queue
 */
class SQSDeleteActor(sqsHelper: SQSHelper) extends Actor with ActorLogging {
  def receive = {
    case SQSMessages(messages) => {
      log.info("deleting messages...")
      sqsHelper.deleteMessages(messages)
    }
  }
}

/**
 * This actor executes the user job for a message,
 * calling the SQSWorker#perform method
 */
class SQSProcessActor(workerInstance: SQSWorker) extends Actor with ActorLogging {
  def receive = {
    case SQSMessage(message) => {
      var successfull = true
      try {
        workerInstance.perform(message)
      } catch {
        //TODO: use a better error handler
        case e: Throwable => {
          successfull = false
          log.error(e.toString())
        }
      }
      sender ! SQSProcessDone(message, successfull)
    }
  }
}

/**
 * This actor receives a batch of messages and
 * send each message to the SQSProcessActor in parallel
 * After all messages got processed, it notifies the manager actor
 */
class SQSBatchActor(processor: ActorRef) extends Actor with ActorLogging {

  implicit val timeout = Timeout(5 seconds)

  def receive = {
    case SQSMessages(messages) => {
      log.info("processing batch...")
      val jobs = ArrayBuffer.empty[Future[Any]]
      messages.foreach(message => {
        jobs += processor ? SQSMessage(message)
      })

      val successfullMessages = ArrayBuffer.empty[Message]
      jobs.foreach(job => {
        val processed = Await.result(job, timeout.duration).asInstanceOf[SQSProcessDone]
        if (processed.successfull) {
          successfullMessages += processed.message
        }
      })
      sender ! SQSBatchDone(successfullMessages.toList)
    }
  }

}

/**
 * This is the system's central actor, it bootstraps the
 * engine and controls how and when other actors get called
 */
class SQSManagerActor(workerInstance: SQSWorker, credentials: Credentials) extends Actor with ActorLogging {

  val queueName = workerInstance.config.get("queueName") match {
    case queueName => queueName.get
  }
  val concurrency = workerInstance.config.getOrElse("concurrency", "10").toInt
  val batches = (concurrency / 10) + 1

  val sqsHelper = new SQSHelper(credentials.accessKey, credentials.secretKey, queueName)

  val system = this.context.system
  val processor = system.actorOf(Props(new SQSProcessActor(workerInstance)).withRouter(RoundRobinRouter(nrOfInstances = concurrency)))
  val fetcher = system.actorOf(Props(new SQSFetchActor(sqsHelper)).withRouter(RoundRobinRouter(nrOfInstances = batches)))
  val batcher = system.actorOf(Props(new SQSBatchActor(processor)).withRouter(RoundRobinRouter(nrOfInstances = batches)))
  val deleter = system.actorOf(Props(new SQSDeleteActor(sqsHelper)).withRouter(RoundRobinRouter(nrOfInstances = batches)))

  sys.ShutdownHookThread {
    stopActors()
  }

  def stopActors() = {
    log.info("stoping actors...")
    context.stop(fetcher);
    context.stop(processor);
    context.stop(batcher);
    context.stop(deleter);
  }

  def receive = {
    case "bootstrap" => {
      for (i <- 1 to batches) fetcher ! "fetch"
    }
    case SQSFetchDone(messages) => {
      batcher ! SQSMessages(messages)
    }
    case SQSBatchDone(messages) => {
      fetcher ! "fetch"
      deleter ! SQSMessages(messages)
    }
  }

}