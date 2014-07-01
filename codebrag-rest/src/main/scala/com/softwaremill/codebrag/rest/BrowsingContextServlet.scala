package com.softwaremill.codebrag.rest

import org.scalatra
import com.softwaremill.codebrag.usecases.{UpdateUserBrowsingContextUseCase, UpdateUserBrowsingContextForm}
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContextFinder
import com.softwaremill.codebrag.service.user.Authenticator

class BrowsingContextServlet(val authenticator: Authenticator, contextFinder: UserBrowsingContextFinder, updateContext: UpdateUserBrowsingContextUseCase) extends JsonServletWithAuthentication {

  get("/") {
    haltIfNotAuthenticated()
    contextFinder.findAll(user.idAsObjectId)
  }

  get("/:repo") {
    haltIfNotAuthenticated()
    val repo = params("repo")
    contextFinder.find(user.idAsObjectId, repo) match {
      case Some(context) => scalatra.Ok(context)
      case None => scalatra.NotFound(Map("error" -> s"Repository $repo found"))
    }
  }

  put("/:repo") {
    haltIfNotAuthenticated()
    val form = UpdateUserBrowsingContextForm(user.idAsObjectId, params("repo"), extractReq[String]("branch"))
    updateContext.execute(form)
  }

}

object BrowsingContextServlet {
  val MappingPath = "browsing-context"
}
