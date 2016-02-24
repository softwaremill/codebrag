package com.softwaremill.codebrag.usecases.reactions

import com.softwaremill.codebrag.common.Clock
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId

/**
 * Handles user activity when user wants to mark all commits as reviewed
 */
class ReviewAllCommitsUseCase
  (implicit clock: Clock) extends Logging {

  type ReviewAllCommitsResult = Either[String, Unit]

  def execute(repoName: String, sha: String, userId: ObjectId): ReviewAllCommitsResult = {
    Left("not implemented!")
  }

}
