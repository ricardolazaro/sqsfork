package com.sqsfork

import com.amazonaws.services.sqs.model.Message

trait SQSWorker {
  
  def config: Map[String, String]

  def perform(message: Message)
   
}