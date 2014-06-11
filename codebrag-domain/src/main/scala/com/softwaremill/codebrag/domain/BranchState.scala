package com.softwaremill.codebrag.domain

case class BranchState(repoName: String, fullBranchName: String, sha: String)

object BranchState extends ((String, String, String) => BranchState)