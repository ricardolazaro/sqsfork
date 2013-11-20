//package com.sqsfork
//
//import akka.actor.ActorSystem
//import com.amazonaws.services.sqs.model.Message
//import akka.actor.ActorLogging
//import akka.actor.Actor
//
//case class SQSMessage(message: Message, sourceUrl: String)
//case class SQSMessages(messages: List[Message])
//case class SQSNoMessage(sourceUrl: String)
//case class SQSMessageForProcess(message: Message, sourceUrl: String)
//case class SQSMessageForDelete(message: Message, sourceUrl: String)
//case class SQSMessageForFutureRetry(messageBody: String)
//
//class SQSManagerActor(accessKey: String, secretKey: String, actorSystem: ActorSystem) extends Actor with ActorLogging {
//  
//  val sqsHelper = new SQSHelper("", "", "")
//  
//  val fetcher = actorSystem.actorOf(Props(new SQSFetchActor(awsManager, queueUrl)).withRouter(RoundRobinRouter(nrOfInstances = SQSManagerActor.NumberOfFetchers))),
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
////    case "bootstrap" => {
////      for (i <- 1 to SQSManagerActor.NumberOfFetchers) fetcher ! "fetch"
////      for (i <- 1 to SQSManagerActor.NumberOfRetryFetchers) retryFetcher ! "fetch"
////    }
////    case SQSMessage(message, sourceUrl) if (sourceUrl == queueUrl) => {
////      fetcher ! "fetch"
////      processor ! SQSMessageForProcess(message, sourceUrl)
////    }
////    case SQSMessage(message, sourceUrl) if (sourceUrl == retryQueueUrl) => {
////      retryFetcher ! "fetch"
////      retryProcessor ! SQSMessageForProcess(message, sourceUrl)
////    }
////    case SQSNoMessage(sourceUrl) if (sourceUrl == queueUrl) => {
////      fetcher ! "fetch"
////    }
////    case SQSNoMessage(sourceUrl) if (sourceUrl == retryQueueUrl) => {
////      retryFetcher ! "fetch"
////    }
//    case msg: SQSMessageForDelete => {
////      remover ! msg
//    }
//  }
//}
//
//
//class SQSFetchActor(sqsHelper: SQSHelper) extends Actor with ActorLogging {
//  def receive = {
//    case "fetch" => {
//      log.info("fetching message...")
//      val messages = sqsHelper.fetchMessages
//      sender ! SQSMessages(messages)
//    }
//  }
//}
//
//
//
