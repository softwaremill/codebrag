package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.dao.heartbeat.HeartbeatDAO
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder

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
    val userId = new ObjectId(user.id)
    heartbeat.update(userId)
    val followupsCount = followupFinder.countFollowupsForUser(userId)
    val toReviewCount = toReviewCommitsFinder.count(userId, extractRepo, extractBranch)
    UpdateNotification(clock.nowMillis, toReviewCount, followupsCount)
  }

  private def extractBranch = params.get("branch")
  private def extractRepo = params.get("repo")

}

object UpdatesServlet {
  val Mapping = "updates"
}

case class UpdateNotification(lastUpdate: Long, commits: Long, followups: Long)