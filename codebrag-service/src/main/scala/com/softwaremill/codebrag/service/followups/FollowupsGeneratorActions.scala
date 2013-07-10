package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.domain.reactions.CommitLiked
import com.softwaremill.codebrag.domain.NewFollowup
import com.softwaremill.codebrag.dao.{CommitInfoDAO, UserDAO, FollowupDAO}
import com.typesafe.scalalogging.slf4j.Logging

trait FollowupsGeneratorActions extends Logging {

  def followupDao: FollowupDAO

  def userDao: UserDAO

  def commitDao: CommitInfoDAO

  def handleCommitLiked(event: CommitLiked) {
    val like = event.like

    val commit = commitDao.findByCommitId(like.commitId).getOrElse(throw new IllegalStateException(s"Commit not found: ${like.commitId}"))
    val commitAuthorOpt = userDao.findByUserName(commit.authorName)

    commitAuthorOpt.foreach(commitAuthor => {
      val followup = NewFollowup(commitAuthor.id, like)
      logger.debug("Generating follow-up for liked commits")
      followupDao.createOrUpdateExisting(followup)
    }
    )
  }
}
