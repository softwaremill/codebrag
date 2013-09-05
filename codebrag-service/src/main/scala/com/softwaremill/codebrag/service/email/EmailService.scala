package com.softwaremill.codebrag.service.email

import com.softwaremill.codebrag.service.config.EmailConfig
import com.softwaremill.codebrag.service.templates.EmailContentWithSubject
import pl.softwaremill.common.sqs.email.EmailSender
import pl.softwaremill.common.sqs.util.EmailDescription

class EmailService(val config: EmailConfig) {

  def send(email:Email) = {
    try {
      val emailDescription = new EmailDescription(email.address, email.content, email.subject)
      EmailSender.send(config.emailSmtpHost, config.emailSmtpPort, config.emailSmtpUserName,
        config.emailSmtpPassword, config.emailFrom, config.emailEncoding, emailDescription)
    } catch {
      case e: Throwable => throw new EmailNotSendException(e.getMessage,e)
    }
  }

}

case class EmailNotSendException(msg: String, e: Throwable) extends RuntimeException(msg: String, e: Throwable)

