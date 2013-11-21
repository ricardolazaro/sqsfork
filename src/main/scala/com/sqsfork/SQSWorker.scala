package com.sqsfork

import com.amazonaws.services.sqs.model.Message
import akka.actor.ActorSystem
import akka.actor.Props
import java.util.concurrent.CountDownLatch

trait SQSWorker {

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

  def config: Map[String, String]

  def perform(message: Message)

}