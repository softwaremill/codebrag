package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import org.joda.time.DateTime


case class Followup(commitId: ObjectId, userId: ObjectId, date: DateTime)

case class ThreadAwareFollowup(id: Option[ObjectId], commitId: ObjectId, userId: ObjectId, date: DateTime, threadId: CommentThreadId) {

  def isOwner(userId: ObjectId) = {
    this.userId == this.userId
  }

}

object ThreadAwareFollowup {

  def apply(commitId: ObjectId, userId: ObjectId, date: DateTime, threadId: CommentThreadId) = {
    new ThreadAwareFollowup(None, commitId: ObjectId, userId: ObjectId, date: DateTime, threadId: CommentThreadId)
  }

  def apply(followupId: ObjectId, commitId: ObjectId, userId: ObjectId, date: DateTime, threadId: CommentThreadId) = {
    new ThreadAwareFollowup(Some(followupId), commitId: ObjectId, userId: ObjectId, date: DateTime, threadId: CommentThreadId)
  }

}