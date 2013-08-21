package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import scala.io.Source._

class VersionServlet extends JsonServlet with Logging {

  val commitShaFile = "commit.sha"

  get("/") {
    try {
      val fileStream = Thread.currentThread().getContextClassLoader.getResourceAsStream(commitShaFile)
      val sha = fromInputStream(fileStream).mkString
      Map("commitId" -> sha.trim)
    } catch {
      case e: Exception => {
        logger.error(s"Could not read ${commitShaFile}", e)
        Map("error" -> s"Could not read commit SHA for this build")
      }
    }
  }

}
