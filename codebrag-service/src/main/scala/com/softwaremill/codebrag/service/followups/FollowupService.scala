package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.dao._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain._
import pl.softwaremill.common.util.time.Clock
import scala.Some
import com.typesafe.scalalogging.slf4j.Logging

class FollowupService(followupDao: FollowupDAO, commitInfoDao: CommitInfoDAO, commitCommentDao: CommitCommentDAO, userDao: UserDAO)(implicit clock: Clock)
  extends Logging {

  def deleteUsersFollowup(userId: ObjectId, followupId: ObjectId): Either[String, Unit] = {
    followupDao.findById(followupId) match {
      case Some(followup) => {
        if(followup.isOwner(userId)) {
          Right(followupDao.delete(followupId))
        } else {
          Left("User not allowed to delete followup")
        }
      }
      case None => Left("No such followup for user to remove")
    }
  }

  def generateFollowupsForComment(currentComment: CommentBase) {
    findCommitWithCommentsRelatedTo(currentComment) match {
      case (None, _) => throwException(s"Commit ${currentComment.commitId} not found. Cannot createOrUpdateExisting follow-ups for nonexisting commit")
      case (Some(commit), List()) => throwException(s"No stored comments for commit ${currentComment.commitId}. Cannot createOrUpdateExisting follow-ups for commit without comments")
      case (Some(commit), existingComments) => generateFollowUps(commit, existingComments, currentComment)
    }

    def throwException(message: String) = throw new RuntimeException(message)
  }


  def generateFollowUps(commit: CommitInfo, existingComments: List[CommentBase], currentComment: CommentBase) {
    val followUpCreationDate = clock.currentDateTimeUTC()
    usersToGenerateFollowUpsFor(commit, existingComments, currentComment).foreach(userId => {
      followupDao.createOrUpdateExisting(Followup(commit.id, userId, followUpCreationDate, currentComment.threadId))
    })
  }

  private def findCommitWithCommentsRelatedTo(comment: CommentBase): (Option[CommitInfo], List[CommentBase]) = {
    (commitInfoDao.findByCommitId(comment.commitId), commitCommentDao.findAllCommentsInThreadWith(comment))
  }

  def usersToGenerateFollowUpsFor(commit: CommitInfo, comments: List[CommentBase], currentComment: CommentBase): Set[ObjectId] = {

    def uniqueCommenters: Set[ObjectId] = {
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
      users.filterNot(_.equals(currentComment.authorId))
    }

    withoutCurrentCommentAuthor(addCommitAuthor(uniqueCommenters))
  }

}
