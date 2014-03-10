package com.softwaremill.codebrag.service.commits.reviewed

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.CommitInfo

class UserReviewedCommitsCache {

  // TODO: make use of it and make it configurable (probably same number as cache per branch size)
  private val MaxCommitsHistoryPerUser = 10

  private val commitsPerUser = new scala.collection.mutable.HashMap[ObjectId, List[String]]
  
  def markCommitAsReviewedBy(userId: ObjectId, commit: CommitInfo) {
    commitsPerUser.get(userId) match {
      case Some(reviewedCommits) => reviewedCommits ++ commit.sha
      case None => commitsPerUser.put(userId, List(commit.sha))
    }
  }

  def reviewedByUser(userId: ObjectId) = {
    commitsPerUser.getOrElse(userId, List.empty)
  }

}
