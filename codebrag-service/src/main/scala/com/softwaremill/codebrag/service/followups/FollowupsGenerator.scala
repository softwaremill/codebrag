package com.softwaremill.codebrag.service.followups

import akka.actor.Actor
import com.softwaremill.codebrag.common.EventBus
import com.softwaremill.codebrag.domain.reactions.{UnlikeEvent, LikeEvent}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.followup.{FollowupWithReactionsDAO, FollowupDAO}

class FollowupsGenerator(val followupDao: FollowupDAO, val userDao: UserDAO, val commitInfoDao: CommitInfoDAO, val followupWithReactionsDao: FollowupWithReactionsDAO, val eventBus: EventBus) extends Actor with FollowupsGeneratorActions {

  def receive = {
    case (event: LikeEvent) => handleCommitLiked(event)
    case (event: UnlikeEvent) => handleUnlikeEvent(event)
  }

}
