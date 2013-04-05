package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.dao.{UserDAO, CommitReviewDAO, CommitInfoDAO, FollowupDAO}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{User, CommitInfo, CommitReview, Followup}
import pl.softwaremill.common.util.time.Clock
import com.softwaremill.codebrag.service.comments.command.AddComment

class FollowupService(followupDAO: FollowupDAO, commitInfoDAO: CommitInfoDAO, commitReviewDAO: CommitReviewDAO, userDAO: UserDAO)(implicit clock: Clock) {

  def generateFollowupsForComment(addedComment: AddComment) {
    findCommitWithReview(addedComment.commitId) match {
      case (Some(commit), Some(review)) => generateFollowUps(commit, review, addedComment)
      case (None, _) => throwException(s"Commit ${addedComment.commitId} not found. Cannot createOrUpdateExisting follow-ups for nonexisting commit")
      case (_, None) => throwException(s"Commit review for commit ${addedComment.commitId} not found. Cannot createOrUpdateExisting follow-ups for commit without comments")
    }

    def throwException(message: String) = throw new RuntimeException(message)
  }


  def generateFollowUps(commit: CommitInfo, review: CommitReview, addedComment: AddComment) {
    val followUpCreationDate = clock.currentDateTimeUTC()
    usersToGenerateFollowUpsFor(commit, review, addedComment).distinct.foreach(userId => {
      followupDAO.createOrUpdateExisting(Followup(commit, userId, followUpCreationDate))
    })
  }

  private def findCommitWithReview(commitId: ObjectId): (Option[CommitInfo], Option[CommitReview]) = {
    (commitInfoDAO.findByCommitId(commitId), commitReviewDAO.findById(commitId))
  }

  def usersToGenerateFollowUpsFor(commit: CommitInfo, commitReview: CommitReview, addedComment: AddComment): List[ObjectId] = {

    def uniqueCommenters(): List[ObjectId] = {
      commitReview.comments.map(_.authorId).distinct
    }

    def addCommitAuthor(users: List[ObjectId]): List[ObjectId] = {

      def findCommitAuthorId(): ObjectId = {
        userDAO.findByUserName(commit.authorName) match {
          case Some(user) => user.id
          case None => throw new RuntimeException("Cannot find commit author")
        }
      }

      findCommitAuthorId:: users
    }

    def withoutCurrentCommentAuthor(users: List[ObjectId]): List[ObjectId] = {
      users.filterNot(_.equals(addedComment.authorId))
    }

    withoutCurrentCommentAuthor(addCommitAuthor(uniqueCommenters))
  }

}
