package com.softwaremill.codebrag.service.comments

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.{LikeDAO, CommitInfoDAO, UserDAO}
import com.softwaremill.codebrag.domain.{CommitInfo, Like}

class LikeValidator(val commitDao: CommitInfoDAO, val likeDao: LikeDAO, val userDao: UserDAO) extends UserAlreadyLikedItCheck with UserIsCommitAuthorCheck {

  import LikeValidator._

  def isLikeValid(like: Like) = {
    if(userIsCommitAuthor(like)) {
      Left(UserCantLikeOwnCode)
    } else if(userAlreadyLikedThat(like)) {
      Left(UserCantLikeMultipleTimes)
    } else {
      Right(true)
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
    val commitOpt = commitDao.findByCommitId(like.commitId)
    commitOpt match {
      case Some(commit) => {
        isUserNameSameAsAuthor(commit, like)
      }
      case None => {
        // TODO: how to handle that better? Either or Exception?
        logger.error(s"Cannot find commit ${like.commitId}")
        false
      }
    }
  }

  private def isUserNameSameAsAuthor(commit: CommitInfo, like: Like): Boolean = {
    userDao.findByUserName(commit.authorName) match {
      case Some(user) => user.id == like.authorId
      case None => {
        logger.debug(s"Cannot find user ${commit.authorName}")
        false
      }
    }
  }

}

trait UserAlreadyLikedItCheck {

  def likeDao: LikeDAO

  def userAlreadyLikedThat(like: Like): Boolean = {
    val allForTheSameCode = likeDao.findAllLikesInThreadWith(like)
    allForTheSameCode.find(_.authorId == like.authorId).nonEmpty
  }

}