package com.softwaremill.codebrag.service.notification

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.{ClockSpec, Clock}
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder
import com.softwaremill.codebrag.service.config.{ConfigWithDefault, CodebragConfig}
import com.typesafe.config.ConfigFactory
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView
import org.mockito.Mockito._
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.LastUserNotificationDispatch

class UserNotificationsSenderSpec
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  var notificationService: NotificationService = _
  var userDao: UserDAO = _
  var notificationCountFinder: NotificationCountFinder = _
  
  var sender: UserNotificationsSender = _

  val SomeCommitsAndFollowups = NotificationCountersView(10, 20)
  val NoCommitsAndFollowups = NotificationCountersView(0, 0)

  override def beforeEach() {
    notificationCountFinder = mock[NotificationCountFinder]
    userDao = mock[UserDAO]
    notificationService = mock[NotificationService]
    
    sender = new TestUserNotificationsSender(notificationCountFinder, userDao, notificationService, clock)
  }

  it should "not send notification when user has notifications disabled" in {
    // given
    val user = UserAssembler.randomUser.withEmailNotificationsDisabled().get
    val heartbeats = List((user.id, clock.currentDateTimeUTC.minusHours(1)))
    when(userDao.findById(user.id)).thenReturn(Some(user))

    // when
    sender.sendUserNotifications(heartbeats)

    // then
    verifyZeroInteractions(notificationService)
  }

  it should "not send notification when user has no commits or followups waiting" in {
    // given
    val user = UserAssembler.randomUser.get.copy(notifications = LastUserNotificationDispatch(None, None))
    val heartbeats = List((user.id, clock.currentDateTimeUTC.minusHours(1)))
    when(userDao.findById(user.id)).thenReturn(Some(user))
    when(notificationCountFinder.getCountersSince(heartbeats.head._2, user.id)).thenReturn(NoCommitsAndFollowups)

    // when
    sender.sendUserNotifications(heartbeats)

    // then
    verifyZeroInteractions(notificationService)
  }

  it should "not send daily digest when user has daily digest email disabled" in {
    // given
    val user = UserAssembler.randomUser.withDailyDigestEmailDisabled().get
    val sender = new TestUserNotificationsSender(notificationCountFinder, userDao, notificationService, clock)

    // when
    sender.sendDailyDigest(List(user))

    // then
    verifyZeroInteractions(notificationService)
    verifyZeroInteractions(notificationCountFinder)
  }

  it should "not send daily digest when user has no commits or followups waiting" in {
    // given
    val user = UserAssembler.randomUser.get
    when(notificationCountFinder.getCounters(user.id)).thenReturn(NoCommitsAndFollowups)

    // when
    sender.sendDailyDigest(List(user))

    // then
    verifyZeroInteractions(notificationService)
  }

  it should "send notification when user has commits or followups" in {
    // given
    val user = UserAssembler.randomUser.get
    val heartbeats = List((user.id, clock.currentDateTimeUTC.minusHours(1)))
    when(userDao.findById(user.id)).thenReturn(Some(user))
    when(notificationCountFinder.getCountersSince(heartbeats.head._2, user.id)).thenReturn(SomeCommitsAndFollowups)

    // when
    sender.sendUserNotifications(heartbeats)

    // then
    verify(notificationService).sendCommitsOrFollowupNotification(user, SomeCommitsAndFollowups.pendingCommitCount, SomeCommitsAndFollowups.followupCount)
  }

  it should "send daily digest when user has commits or followups" in {
    // given
    val user = UserAssembler.randomUser.get
    when(userDao.findById(user.id)).thenReturn(Some(user))
    when(notificationCountFinder.getCounters(user.id)).thenReturn(SomeCommitsAndFollowups)

    // when
    sender.sendDailyDigest(List(user))

    // then
    verify(notificationService).sendDailyDigest(user, SomeCommitsAndFollowups.pendingCommitCount, SomeCommitsAndFollowups.followupCount)
  }

  class TestUserNotificationsSender(_notificationCounts: NotificationCountFinder, _userDao: UserDAO, _notificationService: NotificationService, _clock: Clock) extends UserNotificationsSender {
    def notificationCounts = _notificationCounts
    def userDAO = _userDao
    def clock = _clock
    def notificationService = _notificationService

    def config = new CodebragConfig with ConfigWithDefault {
      import collection.JavaConversions._
      val params = Map("codebrag.user-email-notifications.enabled" -> "true")
      def rootConfig = ConfigFactory.parseMap(params)
    }
  }

}
