package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.version.CodebragBuildInfo._

class VersionServlet extends JsonServlet with Logging {


  get("/") {
      Map("build" -> buildSha, "version" -> version, "date" -> buildDate)
  }

}
