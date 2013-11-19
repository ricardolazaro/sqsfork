package com.sqsfork.spec

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import com.sqsfork.SQSWorker
import com.amazonaws.services.sqs.model.Message


class DummyWorker extends SQSWorker {
  
  def config = Map("queueName" -> "test", "concurrency" -> "3")
  
  def perform(message: Message) = {
    
  }
  
}

class SQSWorkerSpec extends FlatSpec with Matchers {
  
  "A Worker" should "has an accessible configuration" in {
    val worker = new DummyWorker
    worker.config should equal (Map("queueName" -> "test", "concurrency" -> "3"))
  }
  
}