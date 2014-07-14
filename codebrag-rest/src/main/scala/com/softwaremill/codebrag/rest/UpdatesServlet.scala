package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.dao.heartbeat.HeartbeatDAO
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContext

class UpdatesServlet(
  val authenticator: Authenticator,
  followupFinder: FollowupFinder,
  heartbeat: HeartbeatDAO,
  toReviewCommitsFinder: ToReviewCommitsFinder,
  clock: Clock) extends JsonServletWithAuthentication {

  before() {
    haltIfNotAuthenticated()
  }

  get("/") {
    val context = UserBrowsingContext(user.id, extractReqUrlParam("repo"), extractReqUrlParam("branch"))
    heartbeat.update(user.id)
    val followupsCount = followupFinder.countFollowupsForUser(user.id)
    val toReviewCount = toReviewCommitsFinder.count(context)
    UpdateNotification(clock.nowMillis, toReviewCount, followupsCount)
  }

}

object UpdatesServlet {
  val Mapping = "updates"
}

case class UpdateNotification(lastUpdate: Long, commits: Long, followups: Long)