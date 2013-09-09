package com.softwaremill.codebrag.service.email

import akka.actor.{Props, ActorRef, ActorSystem}
import com.softwaremill.codebrag.service.templates.EmailContentWithSubject
import com.softwaremill.codebrag.service.email.EmailSenderActor.SendEmail
import scala.concurrent.duration.FiniteDuration
import com.typesafe.scalalogging.slf4j.Logging

class EmailScheduler(actorSystem: ActorSystem, emailSenderActor: ActorRef) extends Logging {


  def scheduleInstant(email:Email): Any = {
    logger.info(s"Email (subject:${email.subject}, address:${email.address} is scheduled to send instantly")
    emailSenderActor ! SendEmail(email, this)
  }

  def scheduleIn60Seconds(email:Email): Any = {
    schedule(email, EmailSenderActor.NextAttemptAfterFailure)
  }

  def schedule(email:Email, delay: FiniteDuration): Any = {
    logger.info(s"Email (subject:${email.subject}, address:${email.address}) is scheduled to send in $delay")
    scheduleForSender(SendEmail(email, this), actorSystem, delay)
  }


  private def scheduleForSender(sendEmail: SendEmail, actorSys: ActorSystem, delay: FiniteDuration) = {
    import actorSys.dispatcher
    actorSys.scheduler.scheduleOnce(delay, emailSenderActor, sendEmail)
  }

}

case class Email(address:String,subject:String,content:String)

object EmailScheduler {
  def createActor(actorSystem: ActorSystem, emailService: EmailService) = {
    actorSystem.actorOf(Props(new EmailSenderActor(emailService)), "emailSender")

  }
}