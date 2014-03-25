package com.softwaremill.codebrag.activities.finders

trait FullBranchNameResolver {

  val FullBranchNamePrefix = "refs/remotes/origin"

  def resolveFullBranchName(branchName: String) = {
    if(branchName.startsWith(FullBranchNamePrefix)) {
      branchName
    } else {
      s"${FullBranchNamePrefix}/${branchName}"
    }
  }

}
