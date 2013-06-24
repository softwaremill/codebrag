package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.domain.{Authentication, User}
import com.softwaremill.codebrag.service.github.CommitReviewTaskGeneratorActions
import com.softwaremill.codebrag.common.EventBus
import com.typesafe.scalalogging.slf4j.Logging

class Authenticator(userDAO: UserDAO, eventBus: EventBus, reviewTaskGenerator: CommitReviewTaskGeneratorActions) extends Logging{

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    logger.debug("authenticating")
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)
    userOpt match {
      case Some(u) => {
        logger.debug("found user")
        if(Authentication.passwordsMatch(nonEncryptedPassword, u.authentication)) {
          logger.debug("passwords match")
          Some(UserJson(u))
        } else {
          logger.debug("password don't match")
          None
        }
      }
      case _ => {
        logger.debug("user not found")
        None
      }
    }
  }

  def findByLogin(login: String): Option[UserJson] = {
    userDAO.findByLowerCasedLogin(login) match {
      case Some(user) => Some(UserJson(user))
      case _ => None
    }
  }

  def authenticateWithToken(token: String): Option[UserJson] = {
    userDAO.findByToken(token).map(UserJson(_))
  }

}
