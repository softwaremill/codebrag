package com.softwaremill.codebrag.dao.followup

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{FollowupWithNoReactions, ThreadDetails, FollowupWithReactions}
import com.softwaremill.codebrag.dao.reaction.{LikeDAO, CommitCommentDAO}

class SQLFollowupWithReactionsDAO(val database: SQLDatabase, commentsDao: CommitCommentDAO, likeDao: LikeDAO)
  extends FollowupWithReactionsDAO with SQLFollowupSchema {

  import database.driver.simple._
  import database._

  def findById(followupId: ObjectId) = db.withTransaction { implicit session =>
    followups.filter(_.id === followupId).firstOption().map(buildResult)
  }

  private def buildResult(followup: SQLFollowup) = {
    val thread = ThreadDetails(followup.threadCommitId, followup.threadLineNumber, followup.threadFileName)
    val allReactions = commentsDao.findAllCommentsForThread(thread) ++ likeDao.findAllLikesForThread(thread)
    if (allReactions.isEmpty) {
      Left(FollowupWithNoReactions(followup.id, followup.receivingUserId, thread))
    } else {
      val lastReaction = determineLastReaction(allReactions)
      Right(FollowupWithReactions(followup.id, followup.receivingUserId, thread, lastReaction, allReactions))
    }
  }

  def findAllContainingReaction(reactionId: ObjectId) = db.withTransaction { implicit session =>
    (for {
      fr <- followupsReactions if fr.reactionId === reactionId
      f <- fr.followup
    } yield f).list().map(buildResult)
  }

  def update(followup: FollowupWithReactions) {
    db.withTransaction { implicit session =>
      val followupId = followup.followupId
      followups
        .filter(_.id === followupId)
        .map(f => (f.lastReactionId, f.lastReactionAuthor))
        .update(followup.lastReaction.id, followup.lastReaction.authorId)

      val newReactions = followup.allReactions.map(_.id).toSet
      val currentReactions = followupsReactions.filter(_.followupId === followupId).map(_.reactionId).list().toSet
      val reactionsToAdd: Set[ObjectId] = newReactions -- currentReactions
      val reactionsToDelete: Set[ObjectId] = currentReactions -- newReactions

      reactionsToAdd.foreach { r => followupsReactions += (followupId, r) }
      followupsReactions.filter(r => (r.followupId === followupId) && (r.reactionId inSet reactionsToDelete)).delete
    }
  }
}
