package com.softwaremill.codebrag.rest

import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport}
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder
import org.bson.types.ObjectId

class NotificationCountServlet(val authenticator: Authenticator, val swagger: Swagger, finder: NotificationCountFinder) extends JsonServletWithAuthentication with NotificationCountServletSwaggerDefinition with JacksonJsonSupport {

  get("/", operation(getOperation)) {
    haltIfNotAuthenticated()
    finder.getCounters(new ObjectId(user.id))
  }
}

trait NotificationCountServletSwaggerDefinition extends SwaggerSupport {

  override protected val applicationName = Some(NotificationCountServlet.MappingPath)
  protected val applicationDescription: String = "Notification count endpoint"

  val getOperation = apiOperation[NotificationCountersView]("get")
    .summary("Gets counters for notifications about pending commits or follow-ups")
}

object NotificationCountServlet {
  val MappingPath = "notificationCounts"
}