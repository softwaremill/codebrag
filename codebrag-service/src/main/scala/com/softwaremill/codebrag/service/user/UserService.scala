package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.data.UserJson

class UserService(userDAO: UserDAO) {

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    UserJson(userDAO.findByLoginOrEmail(login))
  }

  def authenticateWithToken(token: String): Option[UserJson] = {
    UserJson(userDAO.findByToken(token))
  }

  def findByLogin(login: String): Option[UserJson] = {
    UserJson(userDAO.findByLowerCasedLogin(login))
  }
}
