package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import scala.io.Source._
import com.softwaremill.codebrag.service.config.CodebragConfig

class VersionServlet(config: CodebragConfig) extends JsonServlet with Logging {

  val commitShaFile = "version.id"

  get("/") {
    try {
      val fileStream = Thread.currentThread().getContextClassLoader.getResourceAsStream(commitShaFile)
      val sha = fromInputStream(fileStream).mkString
      Map("build" -> sha.trim, "version" -> config.appVersion)
    } catch {
      case e: Exception => {
        logger.error(s"Could not read ${commitShaFile}", e)
        Map("error" -> s"Could not read commit SHA for this build")
      }
    }
  }

}
