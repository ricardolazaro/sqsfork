package com.sqsfork

import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.Message
import scala.collection.JavaConverters._
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry

/**
 * HeLper to handle SQS Interface ( lots of uggly Java Api )
 */
class SQSHelper(accessKey: String, secretKey: String, queueName: String, endpoint: String) {

  private val client = new AmazonSQSClient(new BasicAWSCredentials(accessKey, secretKey))
  client.setEndpoint(endpoint)

  private lazy val queueUrl = client.createQueue(new CreateQueueRequest(queueName)).getQueueUrl

  def fetchMessages = {
    val request = new ReceiveMessageRequest(queueUrl).withMaxNumberOfMessages(10).withAttributeNames("ApproximateReceiveCount")
    client.receiveMessage(request).getMessages().asScala.toList
  }

  def deleteMessages(messages: List[Message]) = {
    if (messages.nonEmpty) {
      val entries = messages map (message => { new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle()) })
      client.deleteMessageBatch(new DeleteMessageBatchRequest(queueUrl, entries.asJava))
    }
  }

  def send(body: String) = {
    client.sendMessage(new SendMessageRequest(queueUrl, body))
  }

}