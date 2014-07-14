package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.domain.{User, Authentication}
import com.softwaremill.codebrag.common.EventBus
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO

trait Authenticator {

  def userDAO: UserDAO

  def authenticateWithToken(token: String): Option[User] = {
    userDAO.findByToken(token).filter(_.active)
  }

  def findByLogin(login: String): Option[User] = {
    userDAO.findByLowerCasedLogin(login).filter(_.active)
  }

  def authenticate(login: String, nonEncryptedPassword: String): Option[User]

}

class UserPasswordAuthenticator(val userDAO: UserDAO, eventBus: EventBus) extends Authenticator with Logging {

  def authenticate(login: String, nonEncryptedPassword: String): Option[User] = {
    userDAO.findByLoginOrEmail(login).filter { user =>
        user.active && Authentication.passwordsMatch(nonEncryptedPassword, user.authentication)
    }
  }

}

class GitHubEmptyAuthenticator(val userDAO: UserDAO) extends Authenticator with Logging {

  def authenticate(login: String, nonEncryptedPassword: String): Option[User] = {
    userDAO.findByLoginOrEmail(login).filter(_.active)
  }

}
