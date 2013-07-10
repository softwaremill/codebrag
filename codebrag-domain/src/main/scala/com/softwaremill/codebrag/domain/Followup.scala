package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import org.joda.time.DateTime

case class NewFollowup(receivingUserId: ObjectId, reaction: UserReaction)

object NewFollowup {

  def fromOldFollowup(followup: Followup) = {
    val f = followup
    val reaction = followup.followupType match {
      case Followup.FollowupType.Comment => {
        Comment(f.reactionId, f.threadId.commitId, f.authorId, f.date, "", f.threadId.fileName, f.threadId.lineNumber)
      }
      case Followup.FollowupType.Like => {
        Like(f.reactionId, f.threadId.commitId, f.authorId, f.date, f.threadId.fileName, f.threadId.lineNumber)
      }
    }
    new NewFollowup(followup.userId, reaction)
  }

}

@deprecated
// will be deprecated in favor of NewFollowup - better describes what followup is and is lot cleaner
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