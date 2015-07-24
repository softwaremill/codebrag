package com.softwaremill.codebrag.service.user

import java.util.UUID

import com.softwaremill.codebrag.domain.{UserToken, User, Authentication}
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

  def replaceToken(user: User, token: Option[UserToken]) = {
    val newToken = UUID.randomUUID().toString
    val modifiedUser = token match {
      case Some(t) => user.copy(tokens = user.tokens.diff(Set(t)) + UserToken(newToken))
      case None => user.copy(tokens = user.tokens + UserToken(newToken))
    }

    userDAO.modifyUser(modifiedUser)

    newToken
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