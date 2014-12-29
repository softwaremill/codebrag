package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.common.EventBus
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.followup.{FollowupDAO, FollowupWithReactionsDAO}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.reactions.{LikeEvent, UnlikeEvent}
import com.softwaremill.codebrag.domain.{Followup, FollowupForUserCreatedEvent, FollowupWithNoReactions, FollowupWithReactions}
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId

trait FollowupsGeneratorActions extends Logging {

  def followupDao: FollowupDAO
  def userDao: UserDAO
  def commitInfoDao: CommitInfoDAO
  def followupWithReactionsDao: FollowupWithReactionsDAO
  def eventBus: EventBus

  def handleCommitLiked(event: LikeEvent) {
    val like = event.like
    val commit = commitInfoDao.findByCommitId(like.commitId).getOrElse(throw new IllegalStateException(s"Commit not found: ${like.commitId}"))
    val commitAuthorOpt = userDao.findCommitAuthor(commit)
    commitAuthorOpt match {
      case Some(commitAuthor) => {
        val followup = Followup(commitAuthor.id, like)
        logger.debug("Generating follow-up for liked commits")
        followupDao.createOrUpdateExisting(followup)
        eventBus.publish(FollowupForUserCreatedEvent(followup))
      }
      case None => logger.debug("Cannot find commit author for commit " + commit.id)
    }
  }

  def handleUnlikeEvent(event: UnlikeEvent) {
    logger.debug(s"Removing like ${event.like.id}")
    val followupsContainingReaction = followupWithReactionsDao.findAllContainingReaction(event.like.id)
    followupsContainingReaction.foreach(followup => updateOrDeleteFollowup(followup, event.like.id))
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
