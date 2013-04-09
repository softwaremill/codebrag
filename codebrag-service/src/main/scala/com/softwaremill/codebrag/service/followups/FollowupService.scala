package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.dao._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain._
import pl.softwaremill.common.util.time.Clock
import com.softwaremill.codebrag.service.comments.command.AddComment
import com.softwaremill.codebrag.domain.Followup
import scala.Some

class FollowupService(followupDao: FollowupDAO, commitInfoDao: CommitInfoDAO, commitCommentDao: CommitCommentDAO, userDao: UserDAO)(implicit clock: Clock) {

  def generateFollowupsForComment(addedComment: AddComment) {
    findCommitWithComments(addedComment.commitId) match {
      case (None, _) => throwException(s"Commit ${addedComment.commitId} not found. Cannot createOrUpdateExisting follow-ups for nonexisting commit")
      case (Some(commit), List()) => throwException(s"No stored comments for commit ${addedComment.commitId}. Cannot createOrUpdateExisting follow-ups for commit without comments")
      case (Some(commit), currentComments) => generateFollowUps(commit, currentComments, addedComment)
    }

    def throwException(message: String) = throw new RuntimeException(message)
  }


  def generateFollowUps(commit: CommitInfo, currentCommits: List[CommitComment], addedComment: AddComment) {
    val followUpCreationDate = clock.currentDateTimeUTC()
    usersToGenerateFollowUpsFor(commit, currentCommits, addedComment).foreach(userId => {
      followupDao.createOrUpdateExisting(Followup(commit, userId, followUpCreationDate))
    })
  }

  private def findCommitWithComments(commitId: ObjectId): (Option[CommitInfo], List[CommitComment]) = {
    (commitInfoDao.findByCommitId(commitId), commitCommentDao.findAllForCommit(commitId))
  }

  def usersToGenerateFollowUpsFor(commit: CommitInfo, comments: List[CommitComment], addedComment: AddComment): Set[ObjectId] = {

    def uniqueCommenters(): Set[ObjectId] = {
      comments.map(_.authorId).toSet
    }

    def addCommitAuthor(users: Set[ObjectId]): Set[ObjectId] = {

      def findCommitAuthorId(): ObjectId = {
        userDao.findByUserName(commit.authorName) match {
          case Some(user) => user.id
          case None => throw new IllegalStateException(s"Cannot find commit author $commit.authorName")
        }
      }

      users + findCommitAuthorId
    }

    def withoutCurrentCommentAuthor(users: Set[ObjectId]): Set[ObjectId] = {
      users.filterNot(_.equals(addedComment.authorId))
    }

    withoutCurrentCommentAuthor(addCommitAuthor(uniqueCommenters))
  }

}
