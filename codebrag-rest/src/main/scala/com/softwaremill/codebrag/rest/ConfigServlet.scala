package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.service.user.Authenticator

class ConfigServlet(codebragConfig: CodebragConfig, val authenticator: Authenticator) extends JsonServletWithAuthentication with Logging {

  get("/") {
    Map("demo" -> codebragConfig.demo, "emailNotifications" -> codebragConfig.userNotifications)
  }

}
