package com.softwaremill.codebrag

import dao.{MongoFactory, InMemoryFactory}
import rest.CodebragSwagger
import service.config.CodebragConfiguration
import service.schedulers.{DummyEmailSendingService, ProductionEmailSendingService}
import service.templates.EmailTemplatingEngine
import service.user.UserService
import java.util.concurrent.Executors

trait Beans {
  lazy val scheduler = Executors.newScheduledThreadPool(4)

  lazy val daoFactory = sys.props.get("withInMemory") match {
    case Some(value) => new InMemoryFactory
    case None => new MongoFactory
  }

  lazy val emailSendingService = Option(CodebragConfiguration.smtpHost) match {
    case Some(host) => new ProductionEmailSendingService
    case None => new DummyEmailSendingService
  }

  lazy val emailTemplatingEngine = new EmailTemplatingEngine

  lazy val userService = new UserService(userDao, emailSendingService, emailTemplatingEngine)

  lazy val userDao = daoFactory.userDAO

  val swagger = new CodebragSwagger
}
