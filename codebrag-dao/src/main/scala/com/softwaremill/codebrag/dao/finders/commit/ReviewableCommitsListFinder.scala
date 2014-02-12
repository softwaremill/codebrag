package com.softwaremill.codebrag.dao.finders.commit

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.LoadMoreCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskDAO
import com.softwaremill.codebrag.dao.user.UserDAO

class ReviewableCommitsListFinder(
  val commitReviewTaskDAO: CommitReviewTaskDAO,
  val commitInfoDAO: CommitInfoDAO,
  val userDAO: UserDAO) extends CommitsFinder with Logging {

  def findCommitsToReviewFor(userId: ObjectId, paging: LoadMoreCriteria) = {
    val commitsIdsToReview = reviewableCommitsIds(userId)
    findCommits(commitsIdsToReview, paging, identity)
  }

  private def reviewableCommitsIds(userId: ObjectId) = {
    val reviewTasksForUser = commitReviewTaskDAO.commitsPendingReviewFor(userId)
    commitInfoDAO.findPartialCommitInfo(reviewTasksForUser.toList).map(_.id)
  }
}


