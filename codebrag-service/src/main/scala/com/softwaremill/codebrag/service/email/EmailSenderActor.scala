package com.softwaremill.codebrag.service.email

import com.typesafe.scalalogging.slf4j.Logging
import akka.actor.Actor
import com.softwaremill.codebrag.service.email.EmailSenderActor.SendEmail

class EmailSenderActor(emailService: EmailService) extends Actor with Logging {

  def receive = {

    case SendEmail(email: Email, emailScheduler: EmailScheduler) => {
      try {
        if (email.ttl > 0) {
          email.decreaseTtl()
          emailService.send(email)
        }
      } catch {
        case e: EmailNotSendException =>
          logger.error(s"Sending email failed: ${e.getMessage}\n ${e.getCause}")
          if (email.ttl > 0) {
            emailScheduler.schedule10Minutes(email)
          }
      }
    }
  }
}

object EmailSenderActor {

  case class SendEmail(email: Email, emailScheduler: EmailScheduler)

}

