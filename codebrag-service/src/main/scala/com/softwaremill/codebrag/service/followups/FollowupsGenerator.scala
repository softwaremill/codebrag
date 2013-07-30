package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.dao.{CommitInfoDAO, UserDAO, FollowupDAO}
import akka.actor.Actor
import com.softwaremill.codebrag.domain.reactions.LikeEvent

class FollowupsGenerator(followupDaoArg: FollowupDAO, userDaoArg: UserDAO, commitInfoDaoArg: CommitInfoDAO) extends Actor with FollowupsGeneratorActions {

  override def followupDao: FollowupDAO = followupDaoArg
  override def userDao: UserDAO = userDaoArg
  override def commitDao: CommitInfoDAO = commitInfoDaoArg


  def receive = {
    case (event: LikeEvent) => handleCommitLiked(event)
  }

}
