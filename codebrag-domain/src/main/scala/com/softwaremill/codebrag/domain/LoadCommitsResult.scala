package com.softwaremill.codebrag.domain

case class MultibranchLoadCommitsResult(repoName: String, commits: List[CommitsForBranch]) {
  def uniqueCommits = commits.flatMap(_.commits).toSet
}

case class CommitsForBranch(branchName: String, commits: List[CommitInfo], currentBranchSHA: String)