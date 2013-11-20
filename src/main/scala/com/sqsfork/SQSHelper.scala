package com.sqsfork

import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.auth.BasicAWSCredentials
import scala.collection.mutable.Buffer
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.Message
import scala.collection.JavaConverters._


/**
 * HeLper to handle SQS Interface ( lots of uggly Java Api ) 
 */
class SQSHelper(accessKey: String, secretKey: String, queueName: String) {
  
  private val client = new AmazonSQSClient(new BasicAWSCredentials(accessKey, secretKey))
  client.setEndpoint("https://sqs.us-east-1.amazonaws.com/")
  
  private lazy val queueUrl = client.createQueue(new CreateQueueRequest(queueName)).getQueueUrl

  def fetchMessages = {
    val request = new ReceiveMessageRequest(queueUrl)
    	.withMaxNumberOfMessages(10)
    	.withAttributeNames("ApproximateReceiveCount")
    client.receiveMessage(request).getMessages().asScala.toList
  }
    
  def deleteMessage(receiptHandle: String) = {
    client.deleteMessage(new DeleteMessageRequest(queueUrl, receiptHandle))
  }
  
}