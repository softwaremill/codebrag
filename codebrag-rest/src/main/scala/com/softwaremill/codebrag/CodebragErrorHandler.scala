package com.softwaremill.codebrag.rest

import org.scalatra.ScalatraBase
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.licence.LicenceExpiredException
import com.softwaremill.codebrag.usecases.assertions.{ActiveUserStatusRequiredException, AdminRoleRequiredException}
import org.scalatra

trait CodebragErrorHandler extends ScalatraBase with Logging {

  notFound {
    scalatra.NotFound(Map("error" -> "Resource not found"))
  }

  error {
    case e: LicenceExpiredException => halt(402, Map("error" -> e.getMessage))
    case e: AdminRoleRequiredException => {
      logger.debug(s"Permission denied: ${e.getMessage}")
      halt(403, Map("error" -> e.getMessage))
    }
    case e: ActiveUserStatusRequiredException => {
      logger.debug(s"Invalid user status: ${e.getMessage}")
      halt(403, Map("error" -> e.getMessage))
    }
    case e => {
      logger.error("Something went wrong", e)
      halt(500, Map("error" -> "Something went wrong on our side"))
    }
  }

}
