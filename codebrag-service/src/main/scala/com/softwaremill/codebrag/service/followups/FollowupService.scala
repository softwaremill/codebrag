package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.dao.{CommitReviewDAO, CommitInfoDAO, FollowupDAO}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{CommitInfo, CommitReview, Followup}
import pl.softwaremill.common.util.time.Clock

class FollowupService(followupDAO: FollowupDAO, commitInfoDAO: CommitInfoDAO, commitReviewDAO: CommitReviewDAO)(implicit clock: Clock) {

  def generateFollowupsForCommit(commitId: ObjectId) {
    findCommitWithReview(commitId) match {
      case (Some(commit), Some(review)) => {
        findUniqueCommenterIds(review).foreach(userId => {
          followupDAO.createOrUpdateExisting(Followup(commit, userId, clock.currentDateTimeUTC()))
        })
      }
      case (None, _) => throwException(s"Commit ${commitId} not found. Cannot createOrUpdateExisting follow-ups for nonexisting commit")
      case (_, None) => throwException(s"Commit review for commit ${commitId} not found. Cannot createOrUpdateExisting follow-ups for commit without comments")
    }

    def throwException(message: String) = throw new RuntimeException(message)
  }

  private def findCommitWithReview(commitId: ObjectId): (Option[CommitInfo], Option[CommitReview]) = {
    (commitInfoDAO.findByCommitId(commitId), commitReviewDAO.findById(commitId))
  }

  private def findUniqueCommenterIds(commitReview: CommitReview): List[ObjectId] = {
    commitReview.comments.map(_.authorId).distinct
  }

}
