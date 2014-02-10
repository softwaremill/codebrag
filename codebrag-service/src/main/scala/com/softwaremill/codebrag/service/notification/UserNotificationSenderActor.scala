package com.softwaremill.codebrag.service.notification

import akka.actor.{ActorRef, Props, ActorSystem, Actor}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.HeartbeatStore
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView
import com.softwaremill.codebrag.domain.{LastUserNotificationDispatch, User}
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.service.config.CodebragConfig
import scala.concurrent.duration.FiniteDuration
import com.softwaremill.codebrag.common.scheduling.ScheduleDelaysCalculator
import com.softwaremill.codebrag.dao.user.UserDAO

class UserNotificationSenderActor(actorSystem: ActorSystem,
                                  heartbeatStore: HeartbeatStore,
                                  val notificationCounts: NotificationCountFinder,
                                  val userDAO: UserDAO,
                                  val clock: Clock,
                                  val notificationService: NotificationService,
                                  val config: CodebragConfig)
  extends Actor with Logging with UserNotificationsSender {

  import UserNotificationSenderActor._

  def receive = {
    case SendUserNotifications => {
      logger.debug("Preparing notifications to send out")
      sendUserNotifications(heartbeatStore.loadAll())
      scheduleNextNotificationsSendOut(actorSystem, self, config.notificationsCheckInterval)
    }

    case SendDailyDigest => {
      logger.debug("Preparing daily digests to send out")
      scheduleNextDailySendOut(actorSystem, self, clock, config)
      sendDailyDigest(userDAO.findAll())
    }
  }

}

object UserNotificationSenderActor extends Logging {

  def initialize(actorSystem: ActorSystem,
                 heartbeatStore: HeartbeatStore,
                 notificationCountFinder: NotificationCountFinder,
                 userDAO: UserDAO,
                 clock: Clock,
                 notificationsService: NotificationService,
                 config: CodebragConfig) = {
    logger.debug("Initializing user notification system")
    val actor = actorSystem.actorOf(
      Props(new UserNotificationSenderActor(actorSystem, heartbeatStore, notificationCountFinder, userDAO, clock, notificationsService, config)),
      "notification-scheduler")

    scheduleNextNotificationsSendOut(actorSystem, actor, config.notificationsCheckInterval)
    scheduleInitialDailySendOut(actorSystem, actor, clock, config)
  }

  private def scheduleInitialDailySendOut(actorSystem: ActorSystem, receiver: ActorRef, clock: Clock, config: CodebragConfig) {
    import actorSystem.dispatcher
    import scala.concurrent.duration._

    val initialDelay = ScheduleDelaysCalculator.delayToGivenTimeInMillis(config.dailyDigestSendHour, config.dailyDigestSendMinute)(clock).millis
    val dateAtDelay = ScheduleDelaysCalculator.dateAtDelay(clock.currentDateTime, initialDelay)
    logger.debug(s"Scheduling initial daily digest sending at $dateAtDelay")
    actorSystem.scheduler.scheduleOnce(initialDelay, receiver, SendDailyDigest)
  }

  private def scheduleNextDailySendOut(actorSystem: ActorSystem, receiver: ActorRef, clock: Clock, config: CodebragConfig) {
    import actorSystem.dispatcher
    import scala.concurrent.duration._

    val nextSendOutDelay = ScheduleDelaysCalculator.delayInMillis(config.dailyDigestSendInterval)(clock).millis
    val dateAtDelay = clock.currentDateTime.plusMillis(nextSendOutDelay.toMillis.toInt)
    logger.debug(s"Scheduling next daily digest sending at $dateAtDelay")
    actorSystem.scheduler.scheduleOnce(nextSendOutDelay, receiver, SendDailyDigest)
  }

  private def scheduleNextNotificationsSendOut(actorSystem: ActorSystem, receiver: ActorRef, interval: FiniteDuration) {
    import actorSystem.dispatcher
    logger.debug(s"Scheduling next preparation in $interval")
    actorSystem.scheduler.scheduleOnce(interval, receiver, SendUserNotifications)
  }


}

case object SendUserNotifications

case object SendDailyDigest

trait UserNotificationsSender extends Logging {
  def notificationCounts: NotificationCountFinder

  def userDAO: UserDAO

  def clock: Clock

  def notificationService: NotificationService

  def config: CodebragConfig

  def sendUserNotifications(heartbeats: List[(ObjectId, DateTime)]) {
    def userIsOffline(heartbeat: DateTime) = heartbeat.isBefore(clock.currentDateTimeUTC.minus(config.userOfflinePeriod))

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
    val userHasNotificationsEnabled = user.settings.emailNotificationsEnabled
    userHasNotificationsEnabled match {
      case true => {
        val needsCommitNotification = counters.pendingCommitCount > 0 && (user.notifications match {
          case LastUserNotificationDispatch(None, _) => true
          case LastUserNotificationDispatch(Some(date), _) => date.isBefore(heartbeat)
        })
        val needsFollowupNotification = counters.followupCount > 0 && (user.notifications match {
          case LastUserNotificationDispatch(_, None) => true
          case LastUserNotificationDispatch(_, Some(date)) => date.isBefore(heartbeat)
        })
        needsCommitNotification || needsFollowupNotification
      }
      case false => {
        logger.debug(s"Not sending email to ${user.email} - user has notifications disabled")
        false
      }
    }

  }

  private def sendNotifications(user: User, counter: NotificationCountersView) = {
    notificationService.sendCommitsOrFollowupNotification(user, counter.pendingCommitCount, counter.followupCount)
  }

  private def updateLastNotificationsDispatch(user: User, counters: NotificationCountersView) {
    val commitDate = if (counters.pendingCommitCount > 0) Some(clock.currentDateTimeUTC) else None
    val followupDate = if (counters.followupCount > 0) Some(clock.currentDateTimeUTC) else None
    if (commitDate.isDefined || followupDate.isDefined) {
      userDAO.rememberNotifications(user.id, LastUserNotificationDispatch(commitDate, followupDate))
    }
  }

  def sendDailyDigest(users: List[User]) {
    users.foreach {
      user => {
        user.settings.dailyUpdatesEmailEnabled match {
          case true => {
            val counters = notificationCounts.getCounters(user.id)
            if(counters.nonEmpty) {
              notificationService.sendDailyDigest(user, counters.pendingCommitCount, counters.followupCount)
            } else {
              logger.debug(s"Not sending email to ${user.email} - no commits and followups waiting for this user")
            }
          }
          case false => logger.debug(s"Not sending email to ${user.email} - user has daily digest emails disabled")
        }
      }
    }
  }

}
