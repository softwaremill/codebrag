package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.json.JacksonJsonSupport
import org.bson.types.ObjectId
import org.scalatra.NotFound
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.dao.finders.views.SingleFollowupView
import com.softwaremill.codebrag.usecases.reactions.FollowupDoneUseCase

class AllFollowupsServlet(val authenticator: Authenticator,
                       followupFinder: FollowupFinder,
                       followupDoneUseCase: FollowupDoneUseCase)
  extends JsonServletWithAuthentication with JacksonJsonSupport {

  get("/") {
    haltIfNotAuthenticated()
    followupFinder.findAllFollowupsByCommitForDashboard()
  }

  get("/:id") {
    haltIfNotAuthenticated()
    val followupId = params("id")
    followupFinder.findFollowupforDashboard(new ObjectId(followupId)) match {
      case Right(followup) => followup
      case Left(msg) => NotFound(msg)
    }
  }

  delete("/:id") {
    haltIfNotAuthenticated()
    followupDoneUseCase.execute(user.id, new ObjectId(params("id")))
  }
}

object AllFollowupsServlet {
  val MappingPath = "allfollowups"
}
