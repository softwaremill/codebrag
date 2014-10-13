package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.json.JacksonJsonSupport
import org.bson.types.ObjectId
import org.scalatra.NotFound
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.dao.finders.views.SingleFollowupView
import com.softwaremill.codebrag.usecases.reactions.FollowupDoneUseCase

class FollowupsServlet(val authenticator: Authenticator,
                       followupFinder: FollowupFinder,
                       followupDoneUseCase: FollowupDoneUseCase)
  extends JsonServletWithAuthentication with JacksonJsonSupport {

  get("/") {
    haltIfNotAuthenticated()
    followupFinder.findAllFollowupsByCommitForUser(user.id)
  }

  get("/:id") {
    haltIfNotAuthenticated()
    val followupId = params("id")
    followupFinder.findFollowupForUser(user.id, new ObjectId(followupId)) match {
      case Right(followup) => followup
      case Left(msg) => NotFound(msg)
    }
  }

  delete("/:id") {
    haltIfNotAuthenticated()
    followupDoneUseCase.execute(user.id, new ObjectId(params("id")))
  }
}

object FollowupsServlet {
  val MappingPath = "followups"
}