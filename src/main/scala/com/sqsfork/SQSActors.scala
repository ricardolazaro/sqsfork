//package com.sqsfork
//
//import akka.actor.ActorSystem
//import com.amazonaws.services.sqs.model.Message
//import akka.actor.ActorLogging
//import akka.actor.Actor
//
//case class SQSMessage(message: Message, sourceUrl: String)
//case class SQSNoMessage(sourceUrl: String)
//case class SQSMessageForProcess(message: Message, sourceUrl: String)
//case class SQSMessageForDelete(message: Message, sourceUrl: String)
//case class SQSMessageForFutureRetry(messageBody: String)
//
//class SQSManagerActor(executionSettings: ExecutionSettings, actorSystem: ActorSystem) extends Actor with ActorLogging {
//  
//  sys.ShutdownHookThread {
//    stopActors()
//  }
//
//  def stopActors() = {
//    log.info("Stopping actors...")
////    context.stop(fetcher);
//  }
//
//  def receive = {
//    case "bootstrap" => {
//      for (i <- 1 to SQSManagerActor.NumberOfFetchers) fetcher ! "fetch"
//      for (i <- 1 to SQSManagerActor.NumberOfRetryFetchers) retryFetcher ! "fetch"
//    }
//    case SQSMessage(message, sourceUrl) if (sourceUrl == queueUrl) => {
//      fetcher ! "fetch"
//      processor ! SQSMessageForProcess(message, sourceUrl)
//    }
//    case SQSMessage(message, sourceUrl) if (sourceUrl == retryQueueUrl) => {
//      retryFetcher ! "fetch"
//      retryProcessor ! SQSMessageForProcess(message, sourceUrl)
//    }
//    case SQSNoMessage(sourceUrl) if (sourceUrl == queueUrl) => {
//      fetcher ! "fetch"
//    }
//    case SQSNoMessage(sourceUrl) if (sourceUrl == retryQueueUrl) => {
//      retryFetcher ! "fetch"
//    }
//    case msg: SQSMessageForDelete => {
//      remover ! msg
//    }
//  }
//}