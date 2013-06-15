package com.softwaremill.codebrag.service.schedulers

import pl.softwaremill.common.sqs.email.EmailSender
import pl.softwaremill.common.sqs.util.EmailDescription
import javax.mail.MessagingException
import pl.softwaremill.common.sqs.{ ReceivedMessage, Queue, SQS }
import com.google.common.base.Optional
import scala.util.control.Breaks._
import com.softwaremill.codebrag.service.templates.EmailContentWithSubject
import com.softwaremill.codebrag.service.config.{EmailConfig, AwsConfig}

class ProductionEmailSendingService(config: EmailConfig with AwsConfig) extends EmailSendingService {

  val sqsClient = new SQS("queue.amazonaws.com", config.awsAccessKeyId, config.awsSecretAccessKey)
  val emailQueue: Queue = sqsClient.getQueueByName(config.emailTaskSQSQueue)
  emailQueue.setReceiveMessageWaitTime(20)

  def run() {
    logger.debug("Checking emails waiting in the Amazon SQS")
    var messageOpt: Optional[ReceivedMessage] = emailQueue.receiveSingleMessage

    breakable {
      while (messageOpt.isPresent) {
        val message: ReceivedMessage = messageOpt.get()
        val emailToSend: EmailDescription = message.getMessage.asInstanceOf[EmailDescription]

        try {
          EmailSender.send(config.emailSmtpHost, config.emailSmtpPort, config.emailSmtpUserName,
            config.emailSmtpPassword, config.emailFrom, config.emailEncoding, emailToSend)
          logger.info("Email sent!")
          emailQueue.deleteMessage(message)
        } catch {
          case e: MessagingException =>
            logger.error(s"Sending email failed: ${e.getMessage}")
            break()
        }

        messageOpt = emailQueue.receiveSingleMessage
      }
    }
  }

  def scheduleEmail(address: String, emailData: EmailContentWithSubject) {
    emailQueue.sendSerializable(new EmailDescription(address, emailData.content, emailData.subject))
  }
}
