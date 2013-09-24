package com.softwaremill.codebrag.service.email

import akka.actor.{Props, ActorRef, ActorSystem}
import com.softwaremill.codebrag.service.email.EmailSenderActor.SendEmail
import com.typesafe.scalalogging.slf4j.Logging

class EmailScheduler(actorSystem: ActorSystem, emailSenderActor: ActorRef) extends Logging {

  import scala.concurrent.duration._

  val NextAttemptAfterFailure = 10.minutes


  def scheduleInstant(email: Email): Any = {
    logger.info(s"Email (subject:${email.subject}, address:${email.address} is scheduled to send instantly")
    emailSenderActor ! SendEmail(email, this)
  }

  def scheduleIn10Minutes(email: Email): Any = {
    schedule(email, NextAttemptAfterFailure)
  }

  def schedule(email: Email, delay: FiniteDuration): Any = {
    logger.info(s"Email (subject:${email.subject}, address:${email.address}) is scheduled to send in $delay")
    scheduleForSender(SendEmail(email, this), actorSystem, delay)
  }


  private def scheduleForSender(sendEmail: SendEmail, actorSys: ActorSystem, delay: FiniteDuration) = {
    import actorSys.dispatcher
    actorSys.scheduler.scheduleOnce(delay, emailSenderActor, sendEmail)
  }

}

case class Email(address: String, subject: String, content: String, var ttl: Int = 10) {

  def -- = {
    Email(address, subject, content, ttl - 1)
  }

  def shouldSend: Boolean = ttl > 0
}

object EmailScheduler {
  def createActor(actorSystem: ActorSystem, emailService: EmailService) = {
    actorSystem.actorOf(Props(new EmailSenderActor(emailService)), "emailSender")

  }
}