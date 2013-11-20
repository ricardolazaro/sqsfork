package com.sqsfork

import com.amazonaws.services.sqs.model.Message

trait SQSWorker {
  
  def run(implicit credentials: Credentials, waitForever: Boolean) {
    
  }
  
  def config: Map[String, String]

  def perform(message: Message)
   
}