package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.dao._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain._
import pl.softwaremill.common.util.time.Clock
import com.softwaremill.codebrag.service.comments.command.AddComment
import com.softwaremill.codebrag.domain.Followup
import scala.Some
import com.typesafe.scalalogging.slf4j.Logging

class FollowupService(followupDao: FollowupDAO, commitInfoDao: CommitInfoDAO, commitCommentDao: CommitCommentDAO, userDao: UserDAO)(implicit clock: Clock)
  extends Logging {

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

      val authorIdOpt = userDao.findByUserName(commit.authorName).map(_.id)
      authorIdOpt match {
        case Some(authorId) => users + authorId
        case None => {
          logger.warn(s"Unknown commit author ${commit.authorName}. No such user registered. Cannot generate follow-up.")
          users
        }
      }
    }

    def withoutCurrentCommentAuthor(users: Set[ObjectId]): Set[ObjectId] = {
      users.filterNot(_.equals(addedComment.authorId))
    }

    withoutCurrentCommentAuthor(addCommitAuthor(uniqueCommenters))
  }

}
