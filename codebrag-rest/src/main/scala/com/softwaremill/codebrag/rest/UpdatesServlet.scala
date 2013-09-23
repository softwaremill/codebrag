package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.joda.time.DateTime

class UpdatesServlet(val authenticator: Authenticator) extends JsonServletWithAuthentication {
  before() {
    haltIfNotAuthenticated()
  }

  get("/") {
    UpdateNotification(new DateTime().getMillis, 0, 0)
  }

  get("/", sinceLastTime) {
    UpdateNotification(new DateTime().getMillis, 10, 5)
  }

  private def sinceLastTime = params.get("since").isDefined
}

object UpdatesServlet {
  val Mapping = "updates"
}

case class UpdateNotification(lastUpdate: Long, commits: Int, followUps: Int)