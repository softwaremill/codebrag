package com.softwaremill.codebrag.domain

import org.joda.time.DateTime
import org.bson.types.ObjectId

case class CommitReview(commitId: ObjectId, comments: List[CommitComment]) {

  def addComment(id: ObjectId, authorId: ObjectId, message: String, time: DateTime): CommitReview = {
    copy(comments = CommitComment(id, authorId, message, time) :: comments)
  }
}
object CommitReview {
  def createWithComment(commitId: ObjectId, commentId: ObjectId, authorId: ObjectId, message: String, time: DateTime) = {
    CommitReview(commitId, List(CommitComment(commentId, authorId, message, time)))
  }
}
case class CommitComment(id: ObjectId, authorId: ObjectId, message: String, postingTime: DateTime)