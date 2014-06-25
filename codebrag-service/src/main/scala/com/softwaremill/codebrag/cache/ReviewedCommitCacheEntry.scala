package com.softwaremill.codebrag.cache

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.ReviewedCommit
import org.joda.time.DateTime

case class UserReviewedRepoCommitsCacheKey(userId: ObjectId, repoName: String)

case class UserReviewedCommitsCacheEntry(userId: ObjectId, repoName: String, commits: Set[ReviewedCommit], toReviewStartDate: DateTime) {

  def toCacheKey = UserReviewedRepoCommitsCacheKey(userId, repoName)

}