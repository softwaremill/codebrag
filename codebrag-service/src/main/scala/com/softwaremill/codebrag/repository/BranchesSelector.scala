package com.softwaremill.codebrag.repository

import org.eclipse.jgit.api.Git
import scala.collection.JavaConversions._

trait BranchesSelector extends BranchListModeSelector {

  self: Repository =>

  import BranchesSelector._

  def remoteBranchesFullNames = {
    remoteBranches.map(_.getName).filterNot(_ == s"${RemoteBranchPrefix}HEAD").toSet
  }


  def findStaleBranchesFullNames(locallyCachedBranches: Set[String]) = {
    val branches = remoteBranchesFullNames
    locallyCachedBranches.filterNot(branches.contains)
  }

  def getCheckedOutBranchFullName = s"${RemoteBranchPrefix}${repo.getBranch}"

  private def remoteBranches = new Git(repo).branchList().setListMode(branchListMode).call().toList

}

object BranchesSelector {
  val RemoteBranchPrefix = "refs/remotes/origin/"
}
