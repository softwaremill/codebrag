package com.softwaremill.codebrag.usecases.reactions

import com.softwaremill.codebrag.common.{EventBus, Clock}
import com.softwaremill.codebrag.domain.reactions.AllCommitsReviewedEvent
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId

/**
 * Handles user activity when user wants to mark all commits as reviewed
 */
class ReviewAllCommitsUseCase(
  eventBus: EventBus
)
  (implicit clock: Clock) extends Logging {

  type ReviewAllCommitsResult = Either[String, Unit]

  def execute(repoName: String, branch: String, userId: ObjectId): ReviewAllCommitsResult = {
    Right{
      eventBus.publish(AllCommitsReviewedEvent(repoName, branch, userId))
    }
  }

}
