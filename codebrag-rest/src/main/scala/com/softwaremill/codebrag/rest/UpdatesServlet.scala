package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.dao.heartbeat.HeartbeatDAO
import com.softwaremill.codebrag.dao.finders.notification.NotificationCountFinder

class UpdatesServlet(val authenticator: Authenticator, finder: NotificationCountFinder, heartbeat: HeartbeatDAO, clock: Clock) extends JsonServletWithAuthentication {
  before() {
    haltIfNotAuthenticated()
  }

  get("/") {
    val userId = new ObjectId(user.id)
    heartbeat.update(userId)
    val counters = finder.getCounters(userId)
    UpdateNotification(clock.nowMillis, counters.pendingCommitCount, counters.followupCount)
  }

}

object UpdatesServlet {
  val Mapping = "updates"
}

case class UpdateNotification(lastUpdate: Long, commits: Long, followups: Long)