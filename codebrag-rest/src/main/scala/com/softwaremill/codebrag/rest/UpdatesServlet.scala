package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.HeartbeatStore

class UpdatesServlet(val authenticator: Authenticator, finder: NotificationCountFinder, heartbeat: HeartbeatStore) extends JsonServletWithAuthentication {
  before() {
    haltIfNotAuthenticated()
  }

  get("/") {
    val userId = new ObjectId(user.id)
    heartbeat.update(userId)
    val counters = finder.getCounters(userId)
    UpdateNotification(new DateTime().getMillis, counters.pendingCommitCount, counters.followupCount)
  }

}

object UpdatesServlet {
  val Mapping = "updates"
}

case class UpdateNotification(lastUpdate: Long, commits: Long, followups: Long)