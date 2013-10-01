package com.softwaremill.codebrag.service.notification

import akka.actor.{Props, ActorSystem, Actor}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.{UserDAO, HeartbeatStore}
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView
import com.softwaremill.codebrag.service.email.{Email, EmailScheduler}

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
    def userHasPendingNotifications(counters: NotificationCountersView) = counters.pendingCommitCount > 0 || counters.followupCount > 0
    def scheduleNotifications(userId: ObjectId, counters: NotificationCountersView) =
      emailScheduler.scheduleInstant(email(userDAO.findById(userId).get.email, counters))
    def email(address: String, counter: NotificationCountersView) = {
      val subject = s"${counter.pendingCommitCount} commits and ${counter.followupCount} followups are waiting for you"
      val content = s"${counter.pendingCommitCount} commits and ${counter.followupCount} followups are waiting for you"
      Email(address, subject, content)
    }

    var emailsScheduled = 0

    heartbeats.foreach {
      case (userId, lastHeartbeat) =>
        if (userIsOffline(lastHeartbeat)) {
          val counters = notificationCounts.getCountersSince(lastHeartbeat, userId)
          if (userHasPendingNotifications(counters)) {
            scheduleNotifications(userId, counters)
            emailsScheduled += 1
          }
        }
    }

    logger.debug(s"Scheduled $emailsScheduled notification emails")
  }

}
