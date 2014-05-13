package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.cache.BranchCommitsCache

class AvailableBranchesServlet(val authenticator: Authenticator, branchCommitsCache: BranchCommitsCache) extends JsonServletWithAuthentication with Logging {

  get("/") {
    val branches = branchCommitsCache.getShortBranchNames.toList.sorted
    Map("branches" -> branches, "current" -> branchCommitsCache.getCheckedOutBranchShortName, "repoType" -> branchCommitsCache.repository.repoData.repoType)
  }

}

object AvailableBranchesServlet {
  val MountPath = "branches"
}