package com.softwaremill.codebrag.rest

import org.scalatra.{NotFound, ScalatraBase}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.licence.LicenceExpiredException
import com.softwaremill.codebrag.activities.exceptions.PermissionDeniedException

trait CodebragErrorHandler extends ScalatraBase with Logging {

  notFound {
    NotFound()
  }

  error {
    case e: LicenceExpiredException => halt(402, Map("error" -> e.getMessage))
    case e: PermissionDeniedException => {
      logger.debug(s"Permission denied: ${e.getMessage}")
      halt(403, Map("error" -> "You are not allowed to perform this action"))
    }
    case e => {
      logger.error("Something went wrong", e)
      halt(500, Map("error" -> "Something went wrong on our side"))
    }
  }

}
