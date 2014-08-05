package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.usecases.branches.{WatchedBranchForm, StopWatchingBranch, StartWatchingBranch, ListRepositoryBranches}
import org.scalatra
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContext

class RepositoryBranchesServlet(val authenticator: Authenticator,
  toReviewCommitsFinder: ToReviewCommitsFinder,
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

  get("/:repo/branches/:branch/count") {
    val bc = UserBrowsingContext(user.id, extractReqUrlParam("repo"), extractReqUrlParam("branch"))
    Map("toReviewCount" -> toReviewCommitsFinder.count(bc))
  }

}

object RepositoryBranchesServlet {
  val MountPath = "repos"
}