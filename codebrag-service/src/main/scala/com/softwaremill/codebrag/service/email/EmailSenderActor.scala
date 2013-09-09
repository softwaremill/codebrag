package com.softwaremill.codebrag.service.email

import com.typesafe.scalalogging.slf4j.Logging
import akka.actor.Actor
import javax.mail.MessagingException
import com.softwaremill.codebrag.service.templates.EmailContentWithSubject
import com.softwaremill.codebrag.service.email.EmailSenderActor.SendEmail

class EmailSenderActor(emailService: EmailService) extends Actor with Logging {

  def receive = {

    case SendEmail(email:Email, emailScheduler: EmailScheduler) => {
      try {
        emailService.send(email)
      } catch {
        case e: EmailNotSendException =>
          logger.error(s"Sending email failed: ${e.getMessage}\n ${e.getCause}")
          emailScheduler.scheduleIn60Seconds(email)
      }
    }
  }
}

object EmailSenderActor {

  case class SendEmail(email:Email, emailScheduler: EmailScheduler)

}

