package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.data.UserJson

class UserService(userDAO: UserDAO) {

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

}
