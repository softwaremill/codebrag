package com.softwaremill.codebrag.domain

case class BranchState(fullBranchName: String, sha: String)

object BranchState extends ((String, String) => BranchState)