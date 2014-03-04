package com.softwaremill.codebrag.domain

case class LoadCommitsResult(commits: List[CommitInfo], repoName: String, currentRepoHeadSHA: String)

case class MultibranchLoadCommitsResult(repoName: String, commits: List[CommitsForBranch])
case class CommitsForBranch(branchName: String, commits: List[CommitInfo], currentBranchSHA: String)