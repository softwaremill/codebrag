package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import org.joda.time.DateTime

case class NewFollowup(receivingUserId: ObjectId, reaction: UserReaction) {

  def isOwner(userId: ObjectId) = {
    this.receivingUserId == userId
  }

}