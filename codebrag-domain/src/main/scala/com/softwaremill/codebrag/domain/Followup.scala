package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import org.joda.time.DateTime

case class Followup(receivingUserId: ObjectId, reaction: UserReaction) {

  def isOwner(userId: ObjectId) = {
    this.receivingUserId == userId
  }

}

case class FollowupWithUpdateableReactions(followupId: ObjectId, ownerId: ObjectId, thread: ThreadDetails, lastReaction: UserReaction, allReactions: List[UserReaction]) {

  def removeReaction(reactionId: ObjectId) = {
    val modifiedReactions = allReactions.filterNot(_.id == reactionId)
    recalculateLastReaction(modifiedReactions) match {
      case Some(newLastReaction) => this.copy(allReactions = modifiedReactions, lastReaction = newLastReaction)
      case None => this.copy(allReactions = modifiedReactions, lastReaction = null)
    }
  }

  def isEmpty = {
    allReactions.isEmpty || lastReaction == null
  }

  private def recalculateLastReaction(modifiedReactions: List[UserReaction]) = {
    modifiedReactions.reduceOption((r1, r2) => if(r1.postingTime.isBefore(r2.postingTime)) r2 else r1)
  }
}