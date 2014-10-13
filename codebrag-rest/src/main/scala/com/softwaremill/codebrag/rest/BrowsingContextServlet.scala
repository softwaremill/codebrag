package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContextFinder
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.usecases.{UpdateUserBrowsingContextForm, UpdateUserBrowsingContextUseCase}

class BrowsingContextServlet(val authenticator: Authenticator, contextFinder: UserBrowsingContextFinder, updateContext: UpdateUserBrowsingContextUseCase) extends JsonServletWithAuthentication {

  get("/") {
    haltIfNotAuthenticated()
    contextFinder.findAll(user.id)
  }

  get("/:repo") {
    haltIfNotAuthenticated()
    val repo = params("repo")
    contextFinder.find(user.id, repo)
  }

  put("/:repo") {
    haltIfNotAuthenticated()
    val form = UpdateUserBrowsingContextForm(user.id, params("repo"), extractReq[String]("branch"))
    updateContext.execute(form)
  }

}

object BrowsingContextServlet {
  val MappingPath = "browsing-context"
}
