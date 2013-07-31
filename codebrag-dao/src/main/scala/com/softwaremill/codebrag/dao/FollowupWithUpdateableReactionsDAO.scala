package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{ThreadDetails, FollowupWithUpdateableReactions}
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId

trait FollowupWithUpdateableReactionsDAO {

  def findById(followupId: ObjectId): Option[FollowupWithUpdateableReactions]

}

class MongoFollowupWithUpdateableReactionsDAO(commentsDao: CommitCommentDAO, likeDao: LikeDAO) extends FollowupWithUpdateableReactionsDAO {

  def findById(followupId: ObjectId) = {
    FollowupRecord.where(_.id eqs followupId).get().map(buildDomainObject(_))
  }

  private def buildDomainObject(followup: FollowupRecord) = {
    val threadRecord = followup.threadId.get
    val thread = ThreadDetails(threadRecord.commitId.get, threadRecord.lineNumber.get, threadRecord.fileName.get)
    val allReactions = commentsDao.findAllCommentsForThread(thread) ++ likeDao.findAllLikesForThread(thread)
    val Some(lastReaction) = allReactions.find(_.id == followup.lastReaction.get.reactionId.get)
    FollowupWithUpdateableReactions(followup.receivingUserId.get, thread, lastReaction, allReactions)
  }

}
