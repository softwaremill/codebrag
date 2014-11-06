package com.softwaremill.codebrag.rest

import org.scalatra.ScalatraBase
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.usecases.assertions.{ActiveUserStatusRequiredException, AdminRoleRequiredException}
import org.scalatra

/**
 * 404 error handler is separated from other handlers because it should be used only in ScalatraServlet
 * not in ScalatraFilter. ScalatraFilter by default goes to next filter in chain when route not found and
 * we need to maintain this one. ScalatraServlet, in contrast should handle 404 as below.
 */
trait CustomNotFoundErrorsHandler extends Logging { self: ScalatraBase =>

  notFound {
    scalatra.NotFound(Map("error" -> "Resource not found"))
  }

}

trait CustomErrorsHandler extends Logging { self: ScalatraBase =>

  error {
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
