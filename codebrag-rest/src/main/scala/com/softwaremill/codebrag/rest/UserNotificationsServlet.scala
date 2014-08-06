package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.dao.heartbeat.HeartbeatDAO
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContext
import com.softwaremill.codebrag.usecases.notifications.FindUserNotifications

class UserNotificationsServlet(val authenticator: Authenticator, findUserNotifications: FindUserNotifications) extends JsonServletWithAuthentication {

  before() {
    haltIfNotAuthenticated()
  }

  get("/") {
    findUserNotifications.execute(user.id)
  }

}

object UserNotificationsServlet {
  val Mapping = "notifications"
}