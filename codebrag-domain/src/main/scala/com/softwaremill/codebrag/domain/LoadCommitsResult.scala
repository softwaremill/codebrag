package com.softwaremill.codebrag.domain

case class LoadCommitsResult(commits: List[CommitInfo], repoName: String, currentRepoHeadSHA: String)