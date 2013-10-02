package com.softwaremill.codebrag.service.notification

import akka.actor.{Props, ActorSystem, Actor}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.{UserDAO, HeartbeatStore}
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView
import com.softwaremill.codebrag.service.email.{Email, EmailScheduler}
import com.softwaremill.codebrag.domain.{UserNotifications, User}
import com.softwaremill.codebrag.service.templates.{Templates, EmailTemplateEngine}
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.common.Clock

class NotificationScheduler(heartbeatStore: HeartbeatStore,
                            val notificationCounts: NotificationCountFinder,
                            val emailScheduler: EmailScheduler,
                            val userDAO: UserDAO,
                            actorSystem: ActorSystem,
                            val templateEngine: EmailTemplateEngine,
                            val config: CodebragConfig,
                            val clock: Clock) extends Actor with Logging with NotificationProducer {
  def receive = {
    case PrepareNotifications => {
      import actorSystem.dispatcher
      logger.debug("Preparing notifications to send out")
      produceNotifications(heartbeatStore.loadAll())
      logger.debug(s"Scheduling next preparation in ${NotificationScheduler.NextNotificationPreparation}")
      actorSystem.scheduler.scheduleOnce(NotificationScheduler.NextNotificationPreparation, self, PrepareNotifications)
    }
  }

}

object NotificationScheduler {

  import scala.concurrent.duration._

  val OfflineOffset = 5
  val NextNotificationPreparation = 15.minutes

  def initialize(actorSystem: ActorSystem, heartbeatStore: HeartbeatStore, notificationCountFinder: NotificationCountFinder,
                 emailScheduler: EmailScheduler, userDAO: UserDAO, templateEngine: EmailTemplateEngine, config: CodebragConfig,
                 clock: Clock) = {
    val actor = actorSystem.actorOf(
      Props(new NotificationScheduler(heartbeatStore, notificationCountFinder, emailScheduler, userDAO, actorSystem, templateEngine, config, clock)),
      "notification-scheduler")

    actor ! PrepareNotifications
  }

}

case object PrepareNotifications

trait NotificationProducer {
  this: NotificationScheduler =>

  def produceNotifications(heartbeats: List[(ObjectId, DateTime)]) {
    def userIsOffline(heartbeat: DateTime) = heartbeat.isBefore(DateTime.now().minusMinutes(NotificationScheduler.OfflineOffset))

    var emailsScheduled = 0

    heartbeats.foreach {
      case (userId, lastHeartbeat) =>
        if (userIsOffline(lastHeartbeat)) {
          val counters = notificationCounts.getCountersSince(lastHeartbeat, userId)
          val user = userDAO.findById(userId).get
          if (userShouldBeNotified(lastHeartbeat, user, counters)) {
            scheduleNotifications(user, counters)
            rememberNotification(user, counters)
            emailsScheduled += 1
          }
        }
    }

    logger.debug(s"Scheduled $emailsScheduled notification emails")
  }

  private def userShouldBeNotified(heartbeat: DateTime, user: User, counters: NotificationCountersView) = {
    val needsCommitNotification = {
      counters.pendingCommitCount > 0 && (user.notifications match {
        case None => true
        case Some(UserNotifications(None, _)) => true
        case Some(UserNotifications(Some(date), _)) => date.isBefore(heartbeat)
      })
    }
    val needsFollowupNotification = {
      counters.followupCount > 0 && (user.notifications match {
        case None => true
        case Some(UserNotifications(_, None)) => true
        case Some(UserNotifications(_, Some(date))) => date.isBefore(heartbeat)
      })
    }

    needsCommitNotification || needsFollowupNotification
  }

  private def scheduleNotifications(user: User, counter: NotificationCountersView) = {
    val subject = {
      val newCommits = s"${counter.pendingCommitCount} new commits"
      val newFollowups = s"${counter.followupCount} followups"
      val notificationCounts = if (counter.pendingCommitCount > 0 && counter.followupCount > 0) s"$newCommits and $newFollowups"
      else if (counter.pendingCommitCount > 0) newCommits
      else newFollowups
      s"Codebrag: $notificationCounts"
    }

    val templateParams = Map(
      "username" -> user.name,
      "commit_followup_message" -> subject,
      "application_url" -> config.applicationUrl
    )
    val email = Email(user.email, subject, templateEngine.getTemplate(Templates.UserNotifications, templateParams).content)

    emailScheduler.scheduleInstant(email)
  }

  private def rememberNotification(user: User, counters: NotificationCountersView) {
    val commitDate = if (counters.pendingCommitCount > 0) Some(clock.currentDateTimeUTC) else None
    val followupDate = if (counters.followupCount > 0) Some(clock.currentDateTimeUTC) else None
    if (commitDate.isDefined || followupDate.isDefined) {
      userDAO.rememberNotifications(user.id, UserNotifications(commitDate, followupDate))
    }
  }


}
