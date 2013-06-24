package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.domain.{Authentication, User}
import com.softwaremill.codebrag.service.github.CommitReviewTaskGeneratorActions
import com.softwaremill.codebrag.common.EventBus
import com.typesafe.scalalogging.slf4j.Logging

trait Authenticator {

  def userDAO: UserDAO

  def authenticateWithToken(token: String): Option[UserJson] = {
    userDAO.findByToken(token).map(UserJson(_))
  }

  def findByLogin(login: String): Option[UserJson] = {
    userDAO.findByLowerCasedLogin(login) match {
      case Some(user) => Some(UserJson(user))
      case _ => None
    }
  }

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson]

}

class UserPasswordAuthenticator(val userDAO: UserDAO, eventBus: EventBus, reviewTaskGenerator: CommitReviewTaskGeneratorActions) extends Authenticator with Logging {

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)
    userOpt match {
      case Some(u) => {
        if(Authentication.passwordsMatch(nonEncryptedPassword, u.authentication)) {
          Some(UserJson(u))
        } else {
          None
        }
      }
      case _ => {
        None
      }
    }
  }

}

class GitHubEmptyAuthenticator(val userDAO: UserDAO) extends Authenticator with Logging {

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)
    userOpt match {
      case Some(u) => Some(UserJson(u))
      case _ => None
    }
  }

}
