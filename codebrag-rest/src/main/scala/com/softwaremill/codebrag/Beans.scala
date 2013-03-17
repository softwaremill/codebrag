package com.softwaremill.codebrag

import dao.{MongoFactory, InMemoryFactory}
import rest.CodebragSwagger
import service.user.UserService

trait Beans {

  lazy val daoFactory = sys.props.get("withInMemory") match {
    case Some(value) => new InMemoryFactory
    case None => new MongoFactory
  }

  lazy val userService = new UserService(userDao)

  lazy val userDao = daoFactory.userDAO

  val swagger = new CodebragSwagger
}
