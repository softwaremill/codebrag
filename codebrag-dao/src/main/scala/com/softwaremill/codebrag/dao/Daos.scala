package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.dao.user._
import com.softwaremill.codebrag.dao.commitinfo.SQLCommitInfoDAO
import com.softwaremill.codebrag.dao.followup._
import com.softwaremill.codebrag.dao.reaction._
import com.softwaremill.codebrag.dao.reviewtask.SQLCommitReviewTaskDAO
import com.softwaremill.codebrag.dao.invitation.SQLInvitationDAO
import com.softwaremill.codebrag.dao.events.SQLEventDAO
import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO
import com.softwaremill.codebrag.dao.finders.followup.SQLFollowupFinder
import com.softwaremill.codebrag.dao.finders.reaction.ReactionFinder
import com.softwaremill.codebrag.dao.finders.StatsEventsFinder
import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.dao.repositorystatus.SQLRepositoryStatusDAO
import com.softwaremill.codebrag.dao.heartbeat.SQLHeartbeatDAO
import com.softwaremill.codebrag.dao.branchsnapshot.SQLBranchStateDAO
import com.softwaremill.codebrag.dao.reviewedcommits.SQLReviewedCommitsDAO
import com.softwaremill.codebrag.dao.repo.SQLUserRepoDetailsDAO
import com.softwaremill.codebrag.dao.branch.SQLWatchedBranchesDao

trait Daos {
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
  lazy val repoStatusDao = new SQLRepositoryStatusDAO(sqlDatabase)
  lazy val branchStateDao = new SQLBranchStateDAO(sqlDatabase)
  lazy val reviewedCommitsDao = new SQLReviewedCommitsDAO(sqlDatabase)
  lazy val heartbeatDao = new SQLHeartbeatDAO(sqlDatabase, clock)
  lazy val instanceParamsDao = new InstanceParamsDAO(sqlDatabase)
  lazy val userRepoDetailsDao = new SQLUserRepoDetailsDAO(sqlDatabase)
  lazy val userAliasDao = new SQLUserAliasDAO(sqlDatabase)
  lazy val userObservedBranchesDao = new SQLWatchedBranchesDao(sqlDatabase)

  lazy val followupFinder = new SQLFollowupFinder(sqlDatabase, userDao)
  lazy val reactionFinder = new ReactionFinder(userDao, commentDao, likeDao)
  lazy val statsFinder = new StatsEventsFinder(eventDao)

  def sqlDatabase: SQLDatabase
  def clock: Clock
}
