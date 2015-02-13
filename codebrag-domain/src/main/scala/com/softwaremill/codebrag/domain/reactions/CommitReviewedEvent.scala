package com.softwaremill.codebrag.domain.reactions

import com.softwaremill.codebrag.domain.CommitInfo
import com.softwaremill.codebrag.common.{Hookable, StatisticEvent, Clock, Event}
import org.bson.types.ObjectId
import org.joda.time.DateTime
import com.sun.org.glassfish.external.statistics.Statistic

/**
 * Describes event when someone reviewed a commit (hit ok without commenting it)
 */
case class CommitReviewedEvent(
    commit: CommitInfo,
    userIdArg: ObjectId
  )(implicit clock: Clock) extends Event with StatisticEvent with Hookable {

  val hookName = "commit-reviewed"

  def eventType = CommitReviewedEvent.EventType

  def timestamp: DateTime = clock.nowUtc

  def userId = userIdArg

  def toEventStream: String = s"User $userIdArg reviewed commit ${commit.sha}"

}

object CommitReviewedEvent {
  val EventType = "CommitReviewed"
}
