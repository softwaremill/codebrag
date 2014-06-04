package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.domain.Authentication
import com.softwaremill.codebrag.common.EventBus
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO

trait Authenticator {

  def userDAO: UserDAO

  def authenticateWithToken(token: String): Option[UserJson] = {
    userDAO.findByToken(token).filter(_.active).map(UserJson.apply)
  }

  def findByLogin(login: String): Option[UserJson] = {
    userDAO.findByLowerCasedLogin(login).filter(_.active).map(UserJson.apply)
  }

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson]

}

class UserPasswordAuthenticator(val userDAO: UserDAO, eventBus: EventBus) extends Authenticator with Logging {

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    userDAO.findByLoginOrEmail(login).filter { user =>
        user.active && Authentication.passwordsMatch(nonEncryptedPassword, user.authentication)
    }.map(UserJson.apply)
  }

}

class GitHubEmptyAuthenticator(val userDAO: UserDAO) extends Authenticator with Logging {

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    userDAO.findByLoginOrEmail(login).filter(_.active).map(UserJson.apply)
  }

}
