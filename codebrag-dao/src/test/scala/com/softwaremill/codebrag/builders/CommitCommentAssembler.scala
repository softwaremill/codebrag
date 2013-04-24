package com.softwaremill.codebrag.builders

import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.CommitComment

class CommitCommentAssembler(var base: CommitComment) {

  def withId(newId: ObjectId) = {
    base = base.copy(id = newId)
    this
  }

  def withCommitId(newId: ObjectId) = {
    base = base.copy(id = newId)
    this
  }

  def withDate(newDate: DateTime) = {
    base = base.copy(postingTime = newDate)
    this
  }

  def get = base

}

object CommitCommentAssembler {

  def regularComment = {
    val base = new CommitComment(new ObjectId, new ObjectId, new ObjectId, "Comment message", DateTime.now)
    new CommitCommentAssembler(base)
  }

  def commentForCommitId(commitId: ObjectId) = {
    val base = new CommitComment(new ObjectId, commitId, new ObjectId, "Comment message", DateTime.now)
    new CommitCommentAssembler(base)
  }

}
