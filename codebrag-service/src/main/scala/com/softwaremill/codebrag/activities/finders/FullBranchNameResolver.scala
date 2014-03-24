package com.softwaremill.codebrag.activities.finders

trait FullBranchNameResolver {

  def resolveFullBranchName(shortName: String) = s"refs/remotes/origin/${shortName}"

}
