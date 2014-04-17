package com.softwaremill.codebrag.repository

import org.eclipse.jgit.api.Git
import scala.collection.JavaConversions._

trait BranchesSelector extends BranchListModeSelector {

  self: Repository =>

  def remoteBranches = {
    val gitRepo = new Git(repo)
    gitRepo.branchList().setListMode(branchListMode).call().map(_.getName).filterNot(_ == "refs/remotes/origin/HEAD").toSet
  }


  def findStaleBranches(locallyCachedBranches: Set[String]) = {
    val branches = remoteBranches
    locallyCachedBranches.filterNot(branches.contains)
  }

}
