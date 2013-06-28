package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import org.joda.time.DateTime


case class Followup(reactionId: ObjectId, authorId: ObjectId, userId: ObjectId, date: DateTime, lastCommenterName: String, threadId: ThreadDetails, followupType: Followup.FollowupType.Value) {

  def isOwner(userId: ObjectId) = {
    this.userId == userId
  }

}

object Followup {

  def forComment(commitId: ObjectId, authorId: ObjectId, userId: ObjectId, date: DateTime, lastCommenterName: String, threadId: ThreadDetails) = {
    new Followup(commitId: ObjectId, authorId: ObjectId, userId: ObjectId, date: DateTime, lastCommenterName, threadId: ThreadDetails, FollowupType.Comment)
  }

  def forLike(commitId: ObjectId, authorId: ObjectId, userId: ObjectId, date: DateTime, lastCommenterName: String, threadId: ThreadDetails) = {
    new Followup(commitId: ObjectId, authorId: ObjectId, userId: ObjectId, date: DateTime, lastCommenterName, threadId: ThreadDetails, FollowupType.Like)
  }

  object FollowupType extends Enumeration {
    type FollowupType = Value
    val Like = Value("Like")
    val Comment = Value("Comment")
  }
}