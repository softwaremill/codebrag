package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.domain.Authentication
import com.softwaremill.codebrag.service.commits.CommitReviewTaskGeneratorActions
import com.softwaremill.codebrag.common.EventBus
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO

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
    val userOpt = userDAO.findByLoginOrEmail(login)
    userOpt.filter(user => Authentication.passwordsMatch(nonEncryptedPassword, user.authentication)).map(UserJson(_))
  }

}

class GitHubEmptyAuthenticator(val userDAO: UserDAO) extends Authenticator with Logging {

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    userDAO.findByLoginOrEmail(login).map(UserJson(_))
  }

}
