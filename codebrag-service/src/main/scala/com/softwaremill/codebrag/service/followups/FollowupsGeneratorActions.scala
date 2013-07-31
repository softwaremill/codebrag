package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.dao._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.Followup
import scala.Some
import com.softwaremill.codebrag.domain.reactions.LikeEvent
import com.softwaremill.codebrag.domain.reactions.UnlikeEvent

trait FollowupsGeneratorActions extends Logging {

  def followupDao: FollowupDAO

  def userDao: UserDAO

  def commitDao: CommitInfoDAO

  def followupWithReactionsDao: FollowupWithUpdateableReactionsDAO

  def handleCommitLiked(event: LikeEvent) {
    val like = event.like

    val commit = commitDao.findByCommitId(like.commitId).getOrElse(throw new IllegalStateException(s"Commit not found: ${like.commitId}"))
    val commitAuthorOpt = userDao.findByUserName(commit.authorName)

    commitAuthorOpt.foreach(commitAuthor => {
      val followup = Followup(commitAuthor.id, like)
      logger.debug("Generating follow-up for liked commits")
      followupDao.createOrUpdateExisting(followup)
    }
    )
  }

  def handleUnlikeEvent(event: UnlikeEvent) {
    logger.debug(s"Removing like ${event.likeId}")
  }
}
