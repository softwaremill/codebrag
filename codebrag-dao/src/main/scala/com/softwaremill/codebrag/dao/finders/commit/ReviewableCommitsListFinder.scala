package com.softwaremill.codebrag.dao.finders.commit

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.CommitListView
import com.softwaremill.codebrag.common.LoadMoreCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskDAO

class ReviewableCommitsListFinder(val commitReviewTaskDAO: CommitReviewTaskDAO, val commitInfoDAO: CommitInfoDAO)
  extends UserDataEnhancer with CommitReviewedByUserMarker with Logging {

  import CommitInfoToViewConverter._
  import ListSliceLoader._
  import OutOfPageCommitCounter._

  def findCommitsToReviewFor(userId: ObjectId, paging: LoadMoreCriteria) = {
    val commitsIdsToReview = reviewableCommitsIds(userId)
    val commitsSlice = loadSliceUsing(paging, commitsIdsToReview, commitInfoDAO.findPartialCommitInfo)
    val commits = toCommitViews(commitsSlice)
    val numOlder = countOlderCommits(commitsIdsToReview.map(_.toString), commits)
    val numNewer = countNewerCommits(commitsIdsToReview.map(_.toString), commits)
    CommitListView(enhanceWithUserData(commits), numOlder, numNewer)
  }

  private def reviewableCommitsIds(userId: ObjectId) = {
    val reviewTasksForUser = commitReviewTaskDAO.commitsPendingReviewFor(userId)
    commitInfoDAO.findPartialCommitInfo(reviewTasksForUser.toList).map(_.id)
  }
}


