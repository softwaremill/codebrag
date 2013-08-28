package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.config.CodebragConfig

class ViewConfigServlet(val codebragConfig: CodebragConfig) extends JsonServlet with Logging {

  get("/") {
    Map("demo" -> codebragConfig.demo)
  }

}
