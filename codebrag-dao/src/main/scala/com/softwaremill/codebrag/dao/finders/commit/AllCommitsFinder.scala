package com.softwaremill.codebrag.dao.finders.commit

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.LoadMoreCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskDAO

class AllCommitsFinder(val commitReviewTaskDAO: CommitReviewTaskDAO, val commitInfoDAO: CommitInfoDAO)
  extends UserDataEnhancer with CommitReviewedByUserMarker with Logging {

  import CommitInfoToViewConverter._
  import ListSliceLoader._
  import OutOfPageCommitCounter._

  def findAllCommits(paging: LoadMoreCriteria, userId: ObjectId) = {
    val allCommitsIds = commitInfoDAO.findAllIds()
    val commitsSlice = loadSliceUsing(paging, allCommitsIds, commitInfoDAO.findPartialCommitInfo)
    val commits = toCommitViews(commitsSlice)
    val numOlder = countOlderCommits(allCommitsIds.map(_.toString), commits)
    val numNewer = countNewerCommits(allCommitsIds.map(_.toString), commits)
    enhanceWithUserData(markAsReviewed(commits, userId).copy(older = numOlder, newer = numNewer))
  }

  def findCommitById(commitId: ObjectId, userId: ObjectId) = {
    val commitOption = commitInfoDAO.findPartialCommitInfo(List(commitId)).headOption
    commitOption match {
      case Some(commit) => {
        Right(markAsReviewed(enhanceWithUserData(toCommitView(commit)), userId))
      }
      case None => Left(s"No such commit ${commitId.toString}")
    }
  }
}
