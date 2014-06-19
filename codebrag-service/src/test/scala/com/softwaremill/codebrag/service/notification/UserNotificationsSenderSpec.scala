package com.softwaremill.codebrag.service.notification

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.{ClockSpec, Clock}
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.typesafe.config.ConfigFactory
import com.softwaremill.codebrag.domain.builder.UserAssembler
import org.mockito.Mockito._
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.LastUserNotificationDispatch
import com.softwaremill.codebrag.common.config.ConfigWithDefault
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder

class UserNotificationsSenderSpec
  extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach with ClockSpec {

  var notificationService: NotificationService = _
  var userDao: UserDAO = _
  var followupFinder: FollowupFinder = _
  var toReviewCommitsFinder: ToReviewCommitsFinder = _

  var sender: UserNotificationsSender = _

  val SomeFollowups = 20
  val NoFollowups = 0
  val SomeCommits = 10
  val NoCommits = 0

  override def beforeEach() {
    followupFinder = mock[FollowupFinder]
    toReviewCommitsFinder = mock[ToReviewCommitsFinder]
    userDao = mock[UserDAO]
    notificationService = mock[NotificationService]
    
    sender = new TestUserNotificationsSender(followupFinder, toReviewCommitsFinder, userDao, notificationService, clock)
  }

  it should "not send notification when user has notifications disabled" in {
    // given
    val user = UserAssembler.randomUser.withEmailNotificationsDisabled().get
    val userHeartbeat = clock.nowUtc.minusHours(1)
    val heartbeats = List((user.id, userHeartbeat))
    when(userDao.findById(user.id)).thenReturn(Some(user))
    when(followupFinder.countFollowupsForUserSince(userHeartbeat, user.id)).thenReturn(1)

    // when
    sender.sendFollowupsNotification(heartbeats)

    // then
    verifyZeroInteractions(notificationService)
  }

  it should "not send notification when user is not active " in {
    // given
    val user = UserAssembler.randomUser.withEmailNotificationsEnabled().withActive(set = false).get
    val userHeartbeat = clock.nowUtc.minusHours(1)
    val heartbeats = List((user.id, userHeartbeat))
    when(userDao.findById(user.id)).thenReturn(Some(user))
    when(followupFinder.countFollowupsForUserSince(userHeartbeat, user.id)).thenReturn(1)

    // when
    sender.sendFollowupsNotification(heartbeats)

    // then
    verifyZeroInteractions(notificationService)
  }

  it should "not send notification when user has no followups waiting" in {
    // given
    val user = UserAssembler.randomUser.get.copy(notifications = LastUserNotificationDispatch(None, None))
    val heartbeats = List((user.id, clock.nowUtc.minusHours(1)))
    when(userDao.findById(user.id)).thenReturn(Some(user))
    when(followupFinder.countFollowupsForUserSince(heartbeats.head._2, user.id)).thenReturn(NoFollowups)

    // when
    sender.sendFollowupsNotification(heartbeats)

    // then
    verifyZeroInteractions(notificationService)
  }

  it should "not send daily digest when user has daily digest email disabled" in {
    // given
    val user = UserAssembler.randomUser.withDailyDigestEmailDisabled().get
    val sender = new TestUserNotificationsSender(followupFinder, toReviewCommitsFinder, userDao, notificationService, clock)

    // when
    sender.sendDailyDigest(List(user))

    // then
    verifyZeroInteractions(notificationService)
    verifyZeroInteractions(followupFinder)
  }

  it should "not send daily digest when user is not active" in {
    // given
    val user = UserAssembler.randomUser.withDailyDigestEmailEnabled().withActive(set = false).get
    val sender = new TestUserNotificationsSender(followupFinder, toReviewCommitsFinder, userDao, notificationService, clock)

    // when
    sender.sendDailyDigest(List(user))

    // then
    verifyZeroInteractions(notificationService)
    verifyZeroInteractions(followupFinder)
  }

  it should "not send daily digest when user has no commits or followups waiting" in {
    // given
    val user = UserAssembler.randomUser.get
    when(followupFinder.countFollowupsForUser(user.id)).thenReturn(NoFollowups)
    when(toReviewCommitsFinder.countForUserRepoAndBranch(user.id)).thenReturn(NoCommits)

    // when
    sender.sendDailyDigest(List(user))

    // then
    verifyZeroInteractions(notificationService)
  }

  it should "send notification when user has new followups" in {
    // given
    val user = UserAssembler.randomUser.get
    val heartbeats = List((user.id, clock.nowUtc.minusHours(1)))
    when(userDao.findById(user.id)).thenReturn(Some(user))
    when(followupFinder.countFollowupsForUserSince(heartbeats.head._2, user.id)).thenReturn(SomeFollowups)

    // when
    sender.sendFollowupsNotification(heartbeats)

    // then
    verify(notificationService).sendFollowupNotification(user, SomeFollowups)
  }

  it should "send daily digest when user has commits or followups" in {
    // given
    val user = UserAssembler.randomUser.get
    when(userDao.findById(user.id)).thenReturn(Some(user))
    when(toReviewCommitsFinder.countForUserRepoAndBranch(user.id)).thenReturn(SomeCommits)
    when(followupFinder.countFollowupsForUser(user.id)).thenReturn(SomeFollowups)

    // when
    sender.sendDailyDigest(List(user))

    // then
    verify(notificationService).sendDailyDigest(user, SomeCommits, SomeFollowups)
  }

  class TestUserNotificationsSender(
    val followupFinder: FollowupFinder,
    val toReviewCommitsFinder: ToReviewCommitsFinder,
    val userDAO: UserDAO,
    val notificationService: NotificationService,
    val clock: Clock) extends UserNotificationsSender {

    def config = new CodebragConfig with ConfigWithDefault {
      import collection.JavaConversions._
      val params = Map("codebrag.user-email-notifications.enabled" -> "true")
      def rootConfig = ConfigFactory.parseMap(params)
    }
  }

}
