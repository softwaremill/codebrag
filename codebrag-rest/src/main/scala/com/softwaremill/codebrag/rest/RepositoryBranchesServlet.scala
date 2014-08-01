package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.usecases.branches.{WatchedBranchForm, StopWatchingBranch, StartWatchingBranch, ListRepositoryBranches}
import org.scalatra

class RepositoryBranchesServlet(val authenticator: Authenticator,
  listRepositoryBranches: ListRepositoryBranches,
  watchBranch: StartWatchingBranch,
  unwatchBranch: StopWatchingBranch) extends JsonServletWithAuthentication with Logging {

  get("/:repo/branches") {
    val repo = extractReqUrlParam("repo")
    listRepositoryBranches.execute(user.id, repo)
  }

  post("/:repo/branches/:branch/watch") {
    val form = WatchedBranchForm(extractReqUrlParam("repo"), extractReqUrlParam("branch"))
    watchBranch.execute(user.id, form) match {
      case Left(errors) => scalatra.BadRequest(Map("errors" -> errors))
      case Right(observedBranch) => observedBranch
    }
  }

  delete("/:repo/branches/:branch/watch") {
    val form = WatchedBranchForm(extractReqUrlParam("repo"), extractReqUrlParam("branch"))
    unwatchBranch.execute(user.id, form) match {
      case Left(errors) => scalatra.BadRequest(Map("errors" -> errors))
      case Right(_) => scalatra.Ok()
    }
  }

}

object RepositoryBranchesServlet {
  val MountPath = "repos"
}