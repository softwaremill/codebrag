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
import com.softwaremill.codebrag.activities.finders.ToReviewCommitsFinder

class UserNotificationSenderActor(actorSystem: ActorSystem,
                                  heartbeatStore: HeartbeatDAO,
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
                 toReviewCommitsFinder: ToReviewCommitsFinder,
                 followupFinder: FollowupFinder,
                 userDAO: UserDAO,
                 clock: Clock,
                 notificationsService: NotificationService,
                 config: CodebragConfig) = {
    logger.debug("Initializing user notification system")
    val actor = actorSystem.actorOf(
      Props(new UserNotificationSenderActor(actorSystem, heartbeatStore, followupFinder, toReviewCommitsFinder, userDAO, clock, notificationsService, config)),
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
          whenNotificationsEnabled(user) {
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

  private def whenNotificationsEnabled(user: User)(action: => Unit) = {
    if(user.settings.emailNotificationsEnabled) {
      action
    } else {
      logger.debug(s"Not sending email to ${user.emailLowerCase} - user has notifications disabled")
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
    users.foreach {
      user => {
        user.settings.dailyUpdatesEmailEnabled match {
          case true => {
            withNonEmptyCountersFor(user) { (followupsCount, commitsCount) =>
              notificationService.sendDailyDigest(user, commitsCount, followupsCount)
            }
          }
          case false => logger.debug(s"Not sending email to ${user.emailLowerCase} - user has daily digest emails disabled")
        }
      }
    }
  }

  private def withNonEmptyCountersFor(user: User)(actionBlock: (Long, Long) => Unit) = {
    val followupsCount = followupFinder.countFollowupsForUser(user.id)
    val toReviewCommitsCount = toReviewCommitsFinder.countForCurrentBranch(user.id)
    if(followupsCount > 0 || toReviewCommitsCount > 0) {
      actionBlock(followupsCount, toReviewCommitsCount)
    } else {
      logger.debug(s"Not sending email to ${user.emailLowerCase} - no commits and followups waiting for this user")
    }
  }

}
