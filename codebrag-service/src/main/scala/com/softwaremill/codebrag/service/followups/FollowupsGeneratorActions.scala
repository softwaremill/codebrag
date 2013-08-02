package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.dao._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.{FollowupWithNoReactions, FollowupWithReactions, Followup}
import scala.Some
import com.softwaremill.codebrag.domain.reactions.LikeEvent
import com.softwaremill.codebrag.domain.reactions.UnlikeEvent
import org.bson.types.ObjectId

trait FollowupsGeneratorActions extends Logging {

  def followupDao: FollowupDAO

  def userDao: UserDAO

  def commitDao: CommitInfoDAO

  def followupWithReactionsDao: FollowupWithReactionsDAO

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
    val followupsContainingReaction = followupWithReactionsDao.findAllContainingReaction(event.likeId)
    followupsContainingReaction.foreach(followup => updateOrDeleteFollowup(followup, event.likeId))
  }

  def updateOrDeleteFollowup(followup: Either[FollowupWithNoReactions, FollowupWithReactions], likeId: ObjectId) {
    followup match {
      case Left(withNoReactions) => {
        logger.debug(s"Removing followup ${withNoReactions.followupId} due to unlike ${likeId}. It was the only reaction")
        followupDao.delete(withNoReactions.followupId)
      }
      case Right(withReactions) => {
        withReactions.removeReaction(likeId) match {
          case Some(modified) => {
            logger.debug(s"Updating followup ${withReactions.followupId} due to unlike ${likeId}")
            followupWithReactionsDao.update(modified)
          }
          case None => {
            logger.debug(s"Removing followup ${withReactions.followupId} due to unlike ${likeId}. It was the only reaction")
            followupDao.delete(withReactions.followupId)
          }
        }
      }
    }
  }
}
