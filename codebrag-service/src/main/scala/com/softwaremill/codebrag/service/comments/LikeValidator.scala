package com.softwaremill.codebrag.service.comments

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.{CommitInfo, Like}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.reaction.LikeDAO

class LikeValidator(val commitDao: CommitInfoDAO, val likeDao: LikeDAO, val userDao: UserDAO)
  extends UserAlreadyLikedItCheck
  with UserIsCommitAuthorCheck
  with UserIsLikeAuthor {

  import LikeValidator._

  def isLikeValid(like: Like): Either[String, Unit] = {
    if(userIsCommitAuthor(like)) {
      Left(UserCantLikeOwnCode)
    } else if(userAlreadyLikedThat(like)) {
      Left(UserCantLikeMultipleTimes)
    } else {
      Right()
    }
  }

  def canUserDoUnlike(userId: ObjectId, likeId: ObjectId): Either[String, Unit] = {
    if(userIsLikeAuthor(userId, likeId)) {
      Right()
    } else {
      Left(LikeValidator.UserIsNotLikeAuthor)
    }
  }

}

object LikeValidator {

  val UserCantLikeMultipleTimes = "User can't like the same code multiple times"
  val UserCantLikeOwnCode = "User can't like own code"
  val UserIsNotLikeAuthor = "User is not like's author or like doesn't exist"

}
trait UserIsCommitAuthorCheck extends Logging {

  def userDao: UserDAO
  def commitDao: CommitInfoDAO

  def userIsCommitAuthor(like: Like) = {
    val Some(commit) = commitDao.findByCommitId(like.commitId)
    isUserNameSameAsAuthor(commit, like)
  }

  private def isUserNameSameAsAuthor(commit: CommitInfo, like: Like): Boolean = {
    val userOpt = userDao.findCommitAuthor(commit)
    userOpt.getOrElse(logger.debug(s"Cannot find user: ${commit.authorName}/${commit.authorEmail}"))
    userOpt.exists(_.id == like.authorId)
  }
}

trait UserAlreadyLikedItCheck {

  def likeDao: LikeDAO

  def userAlreadyLikedThat(like: Like): Boolean = {
    val allForTheSameCode = likeDao.findAllLikesForThread(like.threadId)
    allForTheSameCode.find(_.authorId == like.authorId).nonEmpty
  }

}

trait UserIsLikeAuthor extends Logging {

  def likeDao: LikeDAO

  def userIsLikeAuthor(userId: ObjectId, likeId: ObjectId): Boolean = {
    likeDao.findById(likeId) match {
      case Some(like) => like.authorId.equals(userId)
      case None => {
        logger.warn(s"Can't find like with id ${likeId.toString}")
        false
      }
    }
  }
}