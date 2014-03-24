package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.commits.branches.BranchCommitsCache

class AvailableBranchesServlet(val authenticator: Authenticator, branchCommitsCache: BranchCommitsCache) extends JsonServletWithAuthentication with Logging {

  get("/") {
    val branches = branchCommitsCache.getBranchNames.map(b => trimBranchName(b)).toList.sorted
    Map("branches" -> branches, "current" -> trimBranchName(branchCommitsCache.repository.getCheckedOutBranchName))
  }

  private def trimBranchName(branchName: String) = branchName.substring(branchName.lastIndexOf("/") + 1)

}

object AvailableBranchesServlet {
  val MountPath = "branches"
}