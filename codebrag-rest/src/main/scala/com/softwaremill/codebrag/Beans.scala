package com.softwaremill.codebrag

import dao.MongoUserDAO
import rest.CodebragSwagger
import service.user.UserService

trait Beans {

  lazy val userService = new UserService(userDao)
  lazy val userDao = new MongoUserDAO
  val swagger = new CodebragSwagger
}
