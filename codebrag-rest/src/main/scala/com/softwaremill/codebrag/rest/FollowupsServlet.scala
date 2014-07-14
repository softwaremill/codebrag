package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.swagger.{SwaggerSupport, Swagger}
import org.scalatra.json.JacksonJsonSupport
import org.bson.types.ObjectId
import org.scalatra.NotFound
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.dao.finders.views.SingleFollowupView
import com.softwaremill.codebrag.usecases.FollowupDoneUseCase

class FollowupsServlet(val authenticator: Authenticator,
                       val swagger: Swagger,
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

trait FollowupsServletSwaggerDefinition extends SwaggerSupport {

  override protected val applicationName = Some(FollowupsServlet.MappingPath)
  protected val applicationDescription: String = "Follow-ups endpoint"

  val getSingleOperation = apiOperation[SingleFollowupView]("get")
  val dismissOperation = apiOperation[Unit]("dismiss")
    .summary("Deletes selected follow-up for current user")
    .parameter(pathParam[String]("id").description("Commit identifier").required)
}

object FollowupsServlet {
  val MappingPath = "followups"
}