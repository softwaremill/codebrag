package com.softwaremill.codebrag.service.commits.branches

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.ReviewedCommit
import org.joda.time.DateTime

case class UserReviewedCommitsCacheEntry(userId: ObjectId, commits: Set[ReviewedCommit], toReviewStartDate: DateTime)

object UserReviewedCommitsCacheEntry {
  def forNewlyRegisteredUser(userId: ObjectId, toReviewStartDate: DateTime) = {
    new UserReviewedCommitsCacheEntry(userId, Set.empty, toReviewStartDate)
  }
}
