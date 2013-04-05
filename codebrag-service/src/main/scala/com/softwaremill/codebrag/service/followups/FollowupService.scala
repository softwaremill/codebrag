package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.dao.{CommitReviewDAO, CommitInfoDAO, FollowupDAO}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{CommitInfo, CommitReview, Followup}
import pl.softwaremill.common.util.time.Clock
import com.softwaremill.codebrag.service.comments.command.AddComment

class FollowupService(followupDAO: FollowupDAO, commitInfoDAO: CommitInfoDAO, commitReviewDAO: CommitReviewDAO)(implicit clock: Clock) {

  def generateFollowupsForComment(addedComment: AddComment) {
    findCommitWithReview(addedComment.commitId) match {
      case (Some(commit), Some(review)) => {
        uniqueCommentersWithoutCurrentAuthor(review, addedComment.authorId).foreach(userId => {
          followupDAO.createOrUpdateExisting(Followup(commit, userId, clock.currentDateTimeUTC()))
        })
      }
      case (None, _) => throwException(s"Commit ${addedComment.commitId} not found. Cannot createOrUpdateExisting follow-ups for nonexisting commit")
      case (_, None) => throwException(s"Commit review for commit ${addedComment.commitId} not found. Cannot createOrUpdateExisting follow-ups for commit without comments")
    }

    def throwException(message: String) = throw new RuntimeException(message)
  }


  private def findCommitWithReview(commitId: ObjectId): (Option[CommitInfo], Option[CommitReview]) = {
    (commitInfoDAO.findByCommitId(commitId), commitReviewDAO.findById(commitId))
  }

  private def uniqueCommentersWithoutCurrentAuthor(commitReview: CommitReview, currentCommenter: ObjectId): List[ObjectId] = {
    commitReview.comments.map(_.authorId).distinct.filterNot(_.equals(currentCommenter))
  }

}
