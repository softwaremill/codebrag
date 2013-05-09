package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import org.joda.time.DateTime


case class Followup(id: Option[ObjectId], commentId: ObjectId, userId: ObjectId, date: DateTime, lastCommenterName: String, threadId: ThreadDetails) {

  def isOwner(userId: ObjectId) = {
    this.userId == this.userId
  }

}

object Followup {

  def apply(commitId: ObjectId, userId: ObjectId, date: DateTime, lastCommenterName: String, threadId: ThreadDetails) = {
    new Followup(None, commitId: ObjectId, userId: ObjectId, date: DateTime, lastCommenterName, threadId: ThreadDetails)
  }

  def apply(followupId: ObjectId, commitId: ObjectId, userId: ObjectId, date: DateTime, lastCommenterName: String, threadId: ThreadDetails) = {
    new Followup(Some(followupId), commitId: ObjectId, userId: ObjectId, date: DateTime, lastCommenterName, threadId: ThreadDetails)
  }

}