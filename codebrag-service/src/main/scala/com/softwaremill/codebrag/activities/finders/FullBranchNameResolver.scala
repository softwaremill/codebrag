package com.softwaremill.codebrag.activities.finders

import com.softwaremill.codebrag.repository.BranchesSelector._

trait FullBranchNameResolver {

  def resolveFullBranchName(branchName: String) = {
    if(branchName.startsWith(RemoteBranchPrefix)) {
      branchName
    } else {
      s"${RemoteBranchPrefix}${branchName}"
    }
  }

}
