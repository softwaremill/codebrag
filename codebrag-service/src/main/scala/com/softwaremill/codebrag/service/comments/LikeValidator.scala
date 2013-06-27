package com.softwaremill.codebrag.service.comments

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.{LikeDAO, CommitInfoDAO, UserDAO}
import com.softwaremill.codebrag.domain.{CommitInfo, Like}

class LikeValidator(val commitDao: CommitInfoDAO, val likeDao: LikeDAO, val userDao: UserDAO) extends UserAlreadyLikedItCheck with UserIsCommitAuthorCheck {

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

}

object LikeValidator {

  val UserCantLikeMultipleTimes = "User can't like the same code multiple times"
  val UserCantLikeOwnCode = "User can't like own code"

}
trait UserIsCommitAuthorCheck extends Logging {

  def userDao: UserDAO
  def commitDao: CommitInfoDAO

  def userIsCommitAuthor(like: Like) = {
    val Some(commit) = commitDao.findByCommitId(like.commitId)
    isUserNameSameAsAuthor(commit, like)
  }

  private def isUserNameSameAsAuthor(commit: CommitInfo, like: Like): Boolean = {
    val userOpt = userDao.findByUserName(commit.authorName)
    userOpt.getOrElse(logger.debug(s"Cannot find user ${commit.authorName}"))
    userOpt.exists(_.id == like.authorId)
  }
}

trait UserAlreadyLikedItCheck {

  def likeDao: LikeDAO

  def userAlreadyLikedThat(like: Like): Boolean = {
    val allForTheSameCode = likeDao.findAllLikesInThreadWith(like)
    allForTheSameCode.find(_.authorId == like.authorId).nonEmpty
  }

}