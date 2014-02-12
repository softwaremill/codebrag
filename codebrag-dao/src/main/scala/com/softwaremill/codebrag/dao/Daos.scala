package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.dao.user._
import com.softwaremill.codebrag.dao.commitinfo.{SQLCommitInfoDAO, MongoCommitInfoDAO, CommitInfoDAO}
import com.softwaremill.codebrag.dao.followup._
import com.softwaremill.codebrag.dao.reaction._
import com.softwaremill.codebrag.dao.reviewtask.{SQLCommitReviewTaskDAO, MongoCommitReviewTaskDAO, CommitReviewTaskDAO}
import com.softwaremill.codebrag.dao.invitation.{SQLInvitationDAO, MongoInvitationDAO, InvitationDAO}
import com.softwaremill.codebrag.dao.events.{SQLEventDAO, MongoEventDAO, EventDAO}
import com.softwaremill.codebrag.dao.instance.FileBasedInstanceSettingsDAO
import com.softwaremill.codebrag.dao.finders.notification.{SQLNotificationCountFinder, MongoNotificationCountFinder, NotificationCountFinder}
import com.softwaremill.codebrag.dao.finders.followup.{SQLFollowupFinder, MongoFollowupFinder, FollowupFinder}
import com.softwaremill.codebrag.dao.finders.reaction.ReactionFinder
import com.softwaremill.codebrag.dao.finders.commit.{ReviewableCommitsListFinder, AllCommitsFinder}
import com.softwaremill.codebrag.dao.finders.StatsEventsFinder
import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.common.Clock

trait Daos {
  def userDao: UserDAO
  def internalUserDao: InternalUserDAO
  def commitInfoDao: CommitInfoDAO
  def followupDao: FollowupDAO
  def followupWithReactionsDao: FollowupWithReactionsDAO
  def likeDao: LikeDAO
  def commentDao: CommitCommentDAO
  def commitReviewTaskDao: CommitReviewTaskDAO
  def invitationDao: InvitationDAO
  def eventDao: EventDAO

  lazy val instanceSettingsDao = new FileBasedInstanceSettingsDAO

  def notificationCountFinder: NotificationCountFinder
  def followupFinder: FollowupFinder

  lazy val reactionFinder = new ReactionFinder(userDao, commentDao, likeDao)
  lazy val allCommitsFinder = new AllCommitsFinder(commitReviewTaskDao, commitInfoDao, userDao)
  lazy val reviewableCommitsFinder = new ReviewableCommitsListFinder(commitReviewTaskDao, commitInfoDao, userDao)
  lazy val statsFinder = new StatsEventsFinder(eventDao)
}

trait MongoDaos extends Daos {
  lazy val userDao = new MongoUserDAO
  lazy val internalUserDao = new MongoInternalUserDAO
  lazy val commitInfoDao = new MongoCommitInfoDAO
  lazy val followupDao = new MongoFollowupDAO
  lazy val followupWithReactionsDao = new MongoFollowupWithReactionsDAO(commentDao, likeDao)
  lazy val likeDao = new MongoLikeDAO
  lazy val commentDao = new MongoCommitCommentDAO
  lazy val commitReviewTaskDao = new MongoCommitReviewTaskDAO
  lazy val invitationDao = new MongoInvitationDAO
  lazy val eventDao = new MongoEventDAO

  lazy val notificationCountFinder = new MongoNotificationCountFinder
  lazy val followupFinder = new MongoFollowupFinder
}

trait SQLDaos extends Daos {
  lazy val userDao = new SQLUserDAO(sqlDatabase)
  lazy val internalUserDao = new SQLInternalUserDAO(sqlDatabase)
  lazy val commitInfoDao = new SQLCommitInfoDAO(sqlDatabase)
  lazy val followupDao = new SQLFollowupDAO(sqlDatabase)
  lazy val followupWithReactionsDao = new SQLFollowupWithReactionsDAO(sqlDatabase, commentDao, likeDao)
  lazy val likeDao = new SQLLikeDAO(sqlDatabase)
  lazy val commentDao = new SQLCommitCommentDAO(sqlDatabase)
  lazy val commitReviewTaskDao = new SQLCommitReviewTaskDAO(sqlDatabase, clock)
  lazy val invitationDao = new SQLInvitationDAO(sqlDatabase)
  lazy val eventDao = new SQLEventDAO(sqlDatabase)

  lazy val notificationCountFinder = new SQLNotificationCountFinder(sqlDatabase)
  lazy val followupFinder = new SQLFollowupFinder(sqlDatabase, userDao)

  def sqlDatabase: SQLDatabase
  def clock: Clock
}
