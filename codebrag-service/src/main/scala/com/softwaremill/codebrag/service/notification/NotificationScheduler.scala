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

class NotificationScheduler(heartbeatStore: HeartbeatStore, val notificationCounts: NotificationCountFinder,
                            val emailScheduler: EmailScheduler, val userDAO: UserDAO,
                            actorSystem: ActorSystem) extends Actor with Logging with NotificationProducer {
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

  val OfflineOffset = 1
  val NextNotificationPreparation = 1.minutes

  def initialize(actorSystem: ActorSystem, heartbeatStore: HeartbeatStore, notificationCountFinder: NotificationCountFinder,
                 emailScheduler: EmailScheduler, userDAO: UserDAO) = {
    val actor = actorSystem.actorOf(
      Props(new NotificationScheduler(heartbeatStore, notificationCountFinder, emailScheduler, userDAO, actorSystem)),
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
            scheduleNotifications(user.email, counters)
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

  private def scheduleNotifications(address: String, counter: NotificationCountersView) = {
    val subject = s"${counter.pendingCommitCount} commits and ${counter.followupCount} followups are waiting for you"
    val content = s"${counter.pendingCommitCount} commits and ${counter.followupCount} followups are waiting for you"
    val email = Email(address, subject, content)

    emailScheduler.scheduleInstant(email)
  }

  private def rememberNotification(user: User, counters: NotificationCountersView) {
    val commitDate = if (counters.pendingCommitCount > 0) Some(DateTime.now()) else None
    val followupDate = if (counters.followupCount > 0) Some(DateTime.now()) else None
    if (commitDate.isDefined || followupDate.isDefined) {
      userDAO.rememberNotifications(user.id, UserNotifications(commitDate, followupDate))
    }
  }


}
