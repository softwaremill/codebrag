package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import org.joda.time.DateTime

case class Followup(receivingUserId: ObjectId, reaction: UserReaction) {

  def isOwner(userId: ObjectId) = {
    this.receivingUserId == userId
  }

}

case class FollowupWithUpdateableReactions(followupId: ObjectId, ownerId: ObjectId, thread: ThreadDetails, lastReaction: UserReaction, allReactions: List[UserReaction])