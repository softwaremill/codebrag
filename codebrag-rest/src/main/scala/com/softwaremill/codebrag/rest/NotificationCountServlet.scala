package com.softwaremill.codebrag.rest

import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport}
import com.softwaremill.codebrag.service.user.Authenticator

class NotificationCountServlet(val authenticator: Authenticator, val swagger: Swagger) extends JsonServletWithAuthentication with NotificationCountServletSwaggerDefinition with JacksonJsonSupport {

  get("/", operation(getOperation)) {
    haltIfNotAuthenticated()
    NotificationCountersView(0, 0) // TODO
  }
}

trait NotificationCountServletSwaggerDefinition extends SwaggerSupport {

  override protected val applicationName = Some(NotificationCountServlet.MappingPath)
  protected val applicationDescription: String = "Notification count endpoint"

  val getOperation = apiOperation[NotificationCountersView]("get")
    .summary("Gets counters for notifications about pending commits or follow-ups")
}

case class NotificationCountersView(pendingCommitCount: Int, followupCount: Int)

object NotificationCountServlet {
  val MappingPath = "notificationCounts"
}