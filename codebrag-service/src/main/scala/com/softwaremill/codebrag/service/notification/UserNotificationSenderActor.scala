package com.softwaremill.codebrag.service.notification

import akka.actor.{Props, ActorSystem, Actor}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.{UserDAO, HeartbeatStore}
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView
import com.softwaremill.codebrag.domain.{UserNotifications, User}
import com.softwaremill.codebrag.common.Clock

class UserNotificationSenderActor(actorSystem: ActorSystem,
                                  heartbeatStore: HeartbeatStore,
                                  val notificationCounts: NotificationCountFinder,
                                  val userDAO: UserDAO,
                                  val clock: Clock,
                                  val notificationService: NotificationService)
  extends Actor with Logging with UserNotificationsSender {

  def receive = {
    case SendUserNotifications => {
      import actorSystem.dispatcher
      logger.debug("Preparing notifications to send out")
      sendUserNotifications(heartbeatStore.loadAll())
      logger.debug(s"Scheduling next preparation in ${UserNotificationSenderActor.NextNotificationPreparation}")
      actorSystem.scheduler.scheduleOnce(UserNotificationSenderActor.NextNotificationPreparation, self, SendUserNotifications)
    }
  }

}

object UserNotificationSenderActor {

  import scala.concurrent.duration._

  val OfflineOffset = 5
  val NextNotificationPreparation = 15.minutes

  def initialize(actorSystem: ActorSystem,
                 heartbeatStore: HeartbeatStore,
                 notificationCountFinder: NotificationCountFinder,
                 userDAO: UserDAO,
                 clock: Clock,
                 notificationsService: NotificationService) = {
    val actor = actorSystem.actorOf(
      Props(new UserNotificationSenderActor(actorSystem, heartbeatStore, notificationCountFinder, userDAO, clock, notificationsService)),
      "notification-scheduler")

    actor ! SendUserNotifications
  }

  def offlineDuration = {
    DateTime.now().minusMinutes(UserNotificationSenderActor.OfflineOffset)
  }

}

case object SendUserNotifications

trait UserNotificationsSender extends Logging {
  def notificationCounts: NotificationCountFinder

  def userDAO: UserDAO

  def clock: Clock

  def notificationService: NotificationService

  def sendUserNotifications(heartbeats: List[(ObjectId, DateTime)]) {
    def userIsOffline(heartbeat: DateTime) = heartbeat.isBefore(UserNotificationSenderActor.offlineDuration)

    var emailsScheduled = 0

    heartbeats.foreach {
      case (userId, lastHeartbeat) =>
        if (userIsOffline(lastHeartbeat)) {
          val counters = notificationCounts.getCountersSince(lastHeartbeat, userId)
          userDAO.findById(userId).foreach(user => {
            if (userShouldBeNotified(lastHeartbeat, user, counters)) {
              sendNotifications(user, counters)
              updateLastNotificationsDispatch(user, counters)
              emailsScheduled += 1
            }
          })
        }
    }

    logger.debug(s"Scheduled $emailsScheduled notification emails")
  }

  private def userShouldBeNotified(heartbeat: DateTime, user: User, counters: NotificationCountersView) = {
    val needsCommitNotification = counters.pendingCommitCount > 0 && (user.notifications match {
      case None => true
      case Some(UserNotifications(None, _)) => true
      case Some(UserNotifications(Some(date), _)) => date.isBefore(heartbeat)
    })

    val needsFollowupNotification = counters.followupCount > 0 && (user.notifications match {
      case None => true
      case Some(UserNotifications(_, None)) => true
      case Some(UserNotifications(_, Some(date))) => date.isBefore(heartbeat)
    })

    needsCommitNotification || needsFollowupNotification
  }

  private def sendNotifications(user: User, counter: NotificationCountersView) = {
    notificationService.sendCommitsOrFollowupNotification(user, counter.pendingCommitCount, counter.followupCount)
  }

  private def updateLastNotificationsDispatch(user: User, counters: NotificationCountersView) {
    val commitDate = if (counters.pendingCommitCount > 0) Some(clock.currentDateTimeUTC) else None
    val followupDate = if (counters.followupCount > 0) Some(clock.currentDateTimeUTC) else None
    if (commitDate.isDefined || followupDate.isDefined) {
      userDAO.rememberNotifications(user.id, UserNotifications(commitDate, followupDate))
    }
  }


}
