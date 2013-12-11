package com.sqsfork

import com.amazonaws.services.sqs.model.Message
import akka.actor.ActorSystem
import akka.actor.Props
import java.util.concurrent.CountDownLatch

trait SQSWorker {

  private var sqsHelper: Option[SQSHelper] = None

  def run(implicit credentials: Credentials, waitForever: Boolean) {
    val workerName = this.getClass.getSimpleName
    val system = ActorSystem(workerName)
    val manager = system.actorOf(Props(new SQSManagerActor(this, credentials)))

    manager ! "bootstrap"

    if (waitForever) {
      val job = new CountDownLatch(1)
      job.await
    }
  }

  def send(body: String)(implicit credentials: Credentials) {
    val helper = sqsHelper match {
      case None => {
        sqsHelper = Some(new SQSHelper(credentials.accessKey, credentials.secretKey, this.queueName, this.endpoint))
        sqsHelper.get
      }
      case Some(_) => sqsHelper.get
    }
    helper.send(body)
  }

  final def queueName = {
    this.config.get("queueName") match {
      case Some(valueForQueueName) => valueForQueueName
      case _ => throw new RuntimeException("queueName not provided")
    }
  }

  final def endpoint = {
    this.config.get("endpoint") match {
      case Some(valueForEndpoint) => valueForEndpoint
      case _ => "https://sqs.us-east-1.amazonaws.com/"
    }
  }

  final def concurrency = {
    this.config.get("concurrency") match {
      case Some(valueForConcurrency) => valueForConcurrency
      case _ => "10"
    }
  }

  def config: Map[String, String]

  def perform(message: Message)


}