package com.softwaremill.codebrag.domain

import com.softwaremill.codebrag.common.Event
import org.bson.types.ObjectId
import org.joda.time.DateTime

case class CommitsUpdatedEvent(firstTime: Boolean, newCommits: List[UpdatedCommit]) extends Event

case class UpdatedCommit(id: ObjectId, authorName: String, commitDate: DateTime)

object CommitUpdatedEvent {
  implicit object CommitLikeUpdatedCommitEvent extends CommitLike[UpdatedCommit] {
    override def authorName(commitLike: UpdatedCommit): String = commitLike.authorName
  }
}