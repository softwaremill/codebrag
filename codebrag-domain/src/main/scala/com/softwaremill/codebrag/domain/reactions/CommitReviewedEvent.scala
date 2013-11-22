package com.softwaremill.codebrag.domain.reactions

import com.softwaremill.codebrag.domain.CommitInfo
import com.softwaremill.codebrag.common.{Clock, Event}
import org.bson.types.ObjectId
import org.joda.time.DateTime

/**
 * Describes event when someone reviewed a commit (hit ok without commenting it)
 */
case class CommitReviewedEvent(commit: CommitInfo, userIdArg: ObjectId)(implicit clock: Clock) extends Event {

  def timestamp: DateTime = clock.currentDateTimeUTC

  def userId: Option[ObjectId] = Some(userIdArg)

  def toEventStream: String = s"User $userIdArg reviewed commit ${commit.sha}"

}
