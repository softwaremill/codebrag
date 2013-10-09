package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.dao.UserDAO
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.user.Authenticator

class ConfigServlet(codebragConfig: CodebragConfig, userDAO: UserDAO, val authenticator: Authenticator) extends JsonServletWithAuthentication with Logging {

  get("/") {
    Map("demo" -> codebragConfig.demo)
  }

  get("/user") {
    haltIfNotAuthenticated()
    userDAO.findById(new ObjectId(user.id)) match {
      case Some(user) => Map("emailNotifications" -> user.settings.emailNotificationsEnabled)
      case None =>
    }
  }

  put("/user") {
    haltIfNotAuthenticated()
  }

}
