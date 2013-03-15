package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.service.schedulers.EmailScheduler
import com.softwaremill.codebrag.service.templates.EmailTemplatingEngine
import pl.softwaremill.common.util.RichString
import java.util.UUID

class UserService(userDAO: UserDAO, emailScheduler: EmailScheduler,
                  emailTemplatingEngine: EmailTemplatingEngine) {

  def load(userId: String) = {
    UserJson(userDAO.load(userId))
  }

  def loadAll = {
    UserJson(userDAO.loadAll)
  }

  def count(): Long = {
    userDAO.countItems()
  }

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)
    userOpt match {
      case Some(u) => {
        if (User.passwordsMatch(nonEncryptedPassword, u)) {
          UserJson(userOpt)
        } else {
          None
        }
      }
      case _ => None
    }
  }

  def authenticateWithToken(token: String): Option[UserJson] = {
    UserJson(userDAO.findByToken(token))
  }

  def findByLogin(login: String): Option[UserJson] = {
    UserJson(userDAO.findByLowerCasedLogin(login))
  }

  def findByEmail(email: String): Option[UserJson] = {
    UserJson(userDAO.findByEmail(email.toLowerCase))
  }

  def changeLogin(currentLogin: String, newLogin: String): Either[String, Unit] = {
    findByLogin(newLogin) match {
      case Some(u) => Left("Login is already taken")
      case None => Right(userDAO.changeLogin(currentLogin, newLogin))
    }
  }

  def changeEmail(currentEmail: String, newEmail: String): Either[String, Unit] = {
    findByEmail(newEmail) match {
      case Some(u) => Left("E-mail used by another user")
      case None => Right(userDAO.changeEmail(currentEmail, newEmail))
    }
  }

}
