package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class Followup(receivingUserId: ObjectId, reaction: UserReaction) {

  def isOwner(userId: ObjectId) = {
    this.receivingUserId == userId
  }

}

case class FollowupWithReactions(followupId: ObjectId, ownerId: ObjectId, thread: ThreadDetails, lastReaction: UserReaction, allReactions: List[UserReaction]) {

  def removeReaction(reactionId: ObjectId): Option[FollowupWithReactions] = {
    val modifiedReactions = allReactions.filterNot(_.id == reactionId)
    recalculateLastReaction(modifiedReactions).map { lastReaction =>
      this.copy(allReactions = modifiedReactions, lastReaction = lastReaction)
    }
  }

  private def recalculateLastReaction(modifiedReactions: List[UserReaction]) = {
    modifiedReactions.reduceOption((r1, r2) => if(r1.postingTime.isBefore(r2.postingTime)) r2 else r1)
  }
}

// for cases when all reactions for followup were removed and followup needs to be fetched in order to remove it
case class FollowupWithNoReactions(followupId: ObjectId, ownerId: ObjectId, thread: ThreadDetails)