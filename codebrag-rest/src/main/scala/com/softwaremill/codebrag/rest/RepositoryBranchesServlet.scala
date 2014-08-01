package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.usecases.branches.ListRepositoryBranches

class RepositoryBranchesServlet(val authenticator: Authenticator, listRepositoryBranches: ListRepositoryBranches) extends JsonServletWithAuthentication with Logging {

  get("/:repo/branches") {
    val repo = extractReqUrlParam("repo")
    listRepositoryBranches.execute(user.id, repo)
  }

}

object RepositoryBranchesServlet {
  val MountPath = "repos"
}