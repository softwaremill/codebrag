package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{ThreadDetails, FollowupWithReactions}
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId

trait FollowupWithReactionsDAO {

  def findById(followupId: ObjectId): Option[FollowupWithReactions]

  def findAllContainingReaction(reactionId: ObjectId): List[FollowupWithReactions]

  def update(followup: FollowupWithReactions)
}

class MongoFollowupWithReactionsDAO(commentsDao: CommitCommentDAO, likeDao: LikeDAO) extends FollowupWithReactionsDAO {

  def findById(followupId: ObjectId) = {
    FollowupRecord.where(_.id eqs followupId).get().map(buildDomainObject)
  }

  private def buildDomainObject(followup: FollowupRecord) = {
    val threadRecord = followup.threadId.get
    val thread = ThreadDetails(threadRecord.commitId.get, threadRecord.lineNumber.get, threadRecord.fileName.get)
    val allReactions = commentsDao.findAllCommentsForThread(thread) ++ likeDao.findAllLikesForThread(thread)
    val Some(lastReaction) = allReactions.find(_.id == followup.lastReaction.get.reactionId.get)
    FollowupWithReactions(followup.id.get, followup.receivingUserId.get, thread, lastReaction, allReactions)
  }

  def findAllContainingReaction(reactionId: ObjectId) = {
    FollowupRecord.where(_.reactions contains reactionId).fetch().map(buildDomainObject)
  }

  def update(followup: FollowupWithReactions) {
    FollowupRecord.where(_.id eqs followup.followupId).findAndModify(_.lastReaction.subfield(_.reactionId) setTo followup.lastReaction.id)
      .and(_.lastReaction.subfield(_.reactionAuthorId) setTo followup.lastReaction.authorId)
      .and(_.lastReaction.subfield(_.reactionType) setTo LastReactionRecord.ReactionTypeEnum(followup.lastReaction.reactionType.id))
      .and(_.reactions setTo followup.allReactions.map(_.id)).updateOne(true)
  }

}
