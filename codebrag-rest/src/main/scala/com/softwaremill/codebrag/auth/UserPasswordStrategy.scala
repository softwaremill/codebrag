package com.softwaremill.codebrag.auth

import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy
import com.softwaremill.codebrag.service.user.UserService
import com.softwaremill.codebrag.service.data.UserJson

class UserPasswordStrategy(protected val app: ScalatraBase, login: String, password: String, val userService: UserService) extends ScentryStrategy[UserJson] {

  override def name: String = UserPassword.name

  override def isValid = {
    !login.isEmpty && !password.isEmpty
  }

  override def authenticate() = {
    userService.authenticate(login, password)
  }

}

object UserPassword {

  val name = "UserPassword"

}
