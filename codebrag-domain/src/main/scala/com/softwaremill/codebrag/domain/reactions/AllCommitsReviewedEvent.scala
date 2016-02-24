package com.softwaremill.codebrag.domain.reactions

import com.softwaremill.codebrag.common._
import org.bson.types.ObjectId
import org.joda.time.DateTime

/**
  * Describes event when someone reviewed all commits for given branch at once (hit Mark All Reviewed)
  */
case class AllCommitsReviewedEvent(
  repoName: String,
  branchName: String,
  userIdArg: ObjectId
)(implicit clock: Clock)
  extends Event
  with StatisticEvent
  with Hookable {

  val hookName = "all-commits-reviewed-hook"

  def eventType = AllCommitsReviewedEvent.EventType

  def timestamp: DateTime = clock.nowUtc

  def userId = userIdArg

  def toEventStream: String = s"User $userIdArg reviewed all commits on branch $branchName"

}

object AllCommitsReviewedEvent {
  val EventType = "AllCommitsReviewed"
}
