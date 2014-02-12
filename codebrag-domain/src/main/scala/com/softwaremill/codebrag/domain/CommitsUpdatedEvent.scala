package com.softwaremill.codebrag.domain

import com.softwaremill.codebrag.common.{Clock, Event}
import org.bson.types.ObjectId
import org.joda.time.DateTime

/**
 * Describes event when repo was updated and new commits appeared
 *
 * @param firstTime is the first event of that kind?
 * @param newCommits list of incoming commits
 * @param clock to obtain when event was created
 */
case class CommitsUpdatedEvent(firstTime: Boolean, newCommits: List[UpdatedCommit])(implicit clock: Clock) extends Event {

  def timestamp: DateTime = clock.nowUtc

  def userId: Option[ObjectId] = None

  def toEventStream: String = s"Number of new commits: ${newCommits.size}"

}

case class UpdatedCommit(id: ObjectId, authorName: String, authorEmail: String, commitDate: DateTime)

object CommitUpdatedEvent {
  implicit object CommitLikeUpdatedCommitEvent extends CommitLike[UpdatedCommit] {
    def authorName(commitLike: UpdatedCommit) = commitLike.authorName
    def authorEmail(commitLike: UpdatedCommit) = commitLike.authorEmail
  }
}