package com.softwaremill.codebrag.domain

import org.joda.time.DateTime
import org.bson.types.ObjectId
import pl.softwaremill.common.util.time.Clock

case class CommitReview(commitId: ObjectId, comments: List[CommitComment]) {

  def addComment(id: ObjectId, authorId: ObjectId, message: String, clock: Clock): CommitReview = {
    copy(comments = CommitComment(id, authorId, message, clock) :: comments)
  }
}
object CommitReview {
  def createWithComment(commitId: ObjectId, commentId: ObjectId, authorId: ObjectId, message: String, clock: Clock) = {
    CommitReview(commitId, List(CommitComment(commentId, authorId, message, clock)))
  }
}
case class CommitComment(id: ObjectId, authorId: ObjectId, message: String, postingTime: DateTime)

object CommitComment {
  def apply(id: ObjectId, authorId: ObjectId, message: String, clock: Clock): CommitComment = {
    CommitComment(id, authorId, message, clock.currentDateTimeUTC())
  }
}