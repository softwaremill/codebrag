package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.data.UserJson

class Authenticator(userDAO: UserDAO) {

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    userDAO.findByLoginOrEmail(login).map(UserJson(_))
  }

  def authenticateWithToken(token: String): Option[UserJson] = {
    userDAO.findByToken(token).map(UserJson(_))
  }

  def findByLogin(login: String): Option[UserJson] = {
    userDAO.findByLowerCasedLogin(login).map(UserJson(_))
  }
}
