package com.softwaremill.codebrag.service.notification

import akka.actor.{ActorRef, Props, ActorSystem, Actor}
import com.typesafe.scalalogging.slf4j.Logging
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{LastUserNotificationDispatch, User}
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.service.config.CodebragConfig
import scala.concurrent.duration.FiniteDuration
import com.softwaremill.codebrag.common.scheduling.ScheduleDelaysCalculator
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.heartbeat.HeartbeatDAO
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder
import com.softwaremill.codebrag.usecases.notifications.{FindUserNotifications, UserNotificationsView}

class UserNotificationSenderActor(
  actorSystem: ActorSystem,
  heartbeatStore: HeartbeatDAO,
  val findUserNotifications: FindUserNotifications,
  val followupFinder: FollowupFinder,
  val toReviewCommitsFinder: ToReviewCommitsFinder,
  val userDAO: UserDAO,
  val clock: Clock,
  val notificationService: NotificationService,
  val config: CodebragConfig)
extends Actor with Logging with UserNotificationsSender {

  import UserNotificationSenderActor._

  def receive = {
    case SendFollowupsNotification => {
      logger.debug("Preparing notifications to send out")
      sendFollowupsNotification(heartbeatStore.loadAll())
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
                 heartbeatStore: HeartbeatDAO,
                 findUserNotifications: FindUserNotifications,
                 toReviewCommitsFinder: ToReviewCommitsFinder,
                 followupFinder: FollowupFinder,
                 userDAO: UserDAO,
                 clock: Clock,
                 notificationsService: NotificationService,
                 config: CodebragConfig) = {
    logger.debug("Initializing user notification system")
    val actor = actorSystem.actorOf(
      Props(new UserNotificationSenderActor(actorSystem, heartbeatStore, findUserNotifications, followupFinder, toReviewCommitsFinder, userDAO, clock, notificationsService, config)),
      "notification-scheduler")

    scheduleNextNotificationsSendOut(actorSystem, actor, config.notificationsCheckInterval)
    scheduleInitialDailySendOut(actorSystem, actor, clock, config)
  }

  private def scheduleInitialDailySendOut(actorSystem: ActorSystem, receiver: ActorRef, clock: Clock, config: CodebragConfig) {
    import actorSystem.dispatcher
    import scala.concurrent.duration._

    val initialDelay = ScheduleDelaysCalculator.delayToGivenTimeInMillis(config.dailyDigestSendHour, config.dailyDigestSendMinute)(clock).millis
    val dateAtDelay = ScheduleDelaysCalculator.dateAtDelay(clock.now, initialDelay)
    logger.debug(s"Scheduling initial daily digest sending at $dateAtDelay")
    actorSystem.scheduler.scheduleOnce(initialDelay, receiver, SendDailyDigest)
  }

  private def scheduleNextDailySendOut(actorSystem: ActorSystem, receiver: ActorRef, clock: Clock, config: CodebragConfig) {
    import actorSystem.dispatcher
    import scala.concurrent.duration._

    val nextSendOutDelay = ScheduleDelaysCalculator.delayInMillis(config.dailyDigestSendInterval)(clock).millis
    val dateAtDelay = clock.now.plusMillis(nextSendOutDelay.toMillis.toInt)
    logger.debug(s"Scheduling next daily digest sending at $dateAtDelay")
    actorSystem.scheduler.scheduleOnce(nextSendOutDelay, receiver, SendDailyDigest)
  }

  private def scheduleNextNotificationsSendOut(actorSystem: ActorSystem, receiver: ActorRef, interval: FiniteDuration) {
    import actorSystem.dispatcher
    logger.debug(s"Scheduling next preparation in $interval")
    actorSystem.scheduler.scheduleOnce(interval, receiver, SendFollowupsNotification)
  }


}

case object SendFollowupsNotification

case object SendDailyDigest

trait UserNotificationsSender extends Logging {

  def findUserNotifications: FindUserNotifications
  def followupFinder: FollowupFinder
  def toReviewCommitsFinder: ToReviewCommitsFinder
  def userDAO: UserDAO
  def clock: Clock
  def notificationService: NotificationService
  def config: CodebragConfig

  def sendFollowupsNotification(heartbeats: List[(ObjectId, DateTime)]) {

    def userIsOffline(heartbeat: DateTime) = heartbeat.isBefore(clock.nowUtc.minus(config.userOfflinePeriod))

    var emailsScheduled = 0

    heartbeats.foreach { case (userId, lastHeartbeat) =>
      if (userIsOffline(lastHeartbeat)) {
        userDAO.findById(userId).foreach { user =>
          whenNotificationsAllowed(user) {
            val followupsCount = followupFinder.countFollowupsForUserSince(lastHeartbeat, user.id)
            if (userShouldBeNotified(lastHeartbeat, user, followupsCount)) {
              sendNotifications(user, followupsCount)
              updateLastNotificationsDispatch(user, followupsCount)
              emailsScheduled += 1
            }
          }
        }
      }
    }
    logger.debug(s"Scheduled $emailsScheduled notification emails")
  }

  private def whenNotificationsAllowed(user: User)(action: => Unit) = {
    if(user.settings.emailNotificationsEnabled && user.active) {
      action
    } else {
      logger.debug(s"Not sending email to ${user.emailLowerCase} - user has notifications disabled or is inactive")
    }
  }

  private def userShouldBeNotified(heartbeat: DateTime, user: User, followupsCount: Long) = {
    followupsCount > 0 && (user.notifications match {
      case LastUserNotificationDispatch(_, None) => true
      case LastUserNotificationDispatch(_, Some(date)) => date.isBefore(heartbeat)
    })
  }

  private def sendNotifications(user: User, followupsCount: Long) = {
    notificationService.sendFollowupNotification(user, followupsCount)
  }

  private def updateLastNotificationsDispatch(user: User, followupsCount: Long) {
    val followupDate = if (followupsCount > 0) Some(clock.nowUtc) else None
    followupDate.foreach { date =>
      userDAO.rememberNotifications(user.id, LastUserNotificationDispatch(followupDate, followupDate))
    }
  }

  def sendDailyDigest(users: List[User]) {
    users.filter(_.active).foreach {
      user => {
        user.settings.dailyUpdatesEmailEnabled match {
          case true => {
            withNonEmptyUserNotifications(user)( n => notificationService.sendDailySummary(user, n))
          }
          case false => logger.debug(s"Not sending email to ${user.emailLowerCase} - user has daily digest emails disabled")
        }
      }
    }
  }

  private def withNonEmptyUserNotifications(user: User)(actionBlock: UserNotificationsView => Unit) = {
    val notifications = findUserNotifications.execute(user.id)
    val allWithNonEmptyNotifs = notifications.copy(repos = notifications.repos.filter(_.commits > 0))
    if(allWithNonEmptyNotifs.nonEmpty) {
      actionBlock(allWithNonEmptyNotifs)
    } else {
      logger.debug(s"Not sending email to ${user.emailLowerCase} - no commits and followups waiting for this user")
    }
  }

}
