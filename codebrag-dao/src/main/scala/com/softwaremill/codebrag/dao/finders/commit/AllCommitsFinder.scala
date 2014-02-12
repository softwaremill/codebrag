package com.softwaremill.codebrag.dao.finders.commit

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.LoadMoreCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskDAO
import com.softwaremill.codebrag.dao.user.UserDAO

class AllCommitsFinder(
  val commitReviewTaskDAO: CommitReviewTaskDAO,
  val commitInfoDAO: CommitInfoDAO,
  val userDAO: UserDAO) extends CommitsFinder with CommitReviewedByUserMarker with Logging {

  def findAllCommits(paging: LoadMoreCriteria, userId: ObjectId) = {
    val allCommitsIds = commitInfoDAO.findAllIds()
    findCommits(allCommitsIds, paging, markAsReviewed(_, userId))
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
