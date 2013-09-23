package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator

class UpdatesServlet(val authenticator: Authenticator) extends JsonServletWithAuthentication {
  before() {
    haltIfNotAuthenticated()
  }

  get("/", sinceLastTime) {
    UpdateNotification(params.get("since").get.toLong, 10, 5)
  }

  private def sinceLastTime = params.get("since").isDefined
}

object UpdatesServlet {
  val Mapping = "updates"
}

case class UpdateNotification(lastUpdate: Long, commits: Int, followUps: Int)