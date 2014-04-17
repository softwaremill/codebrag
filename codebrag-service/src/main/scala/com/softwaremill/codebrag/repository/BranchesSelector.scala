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

  def getCheckedOutBranchFullName = {
    val shortBranchName = repo.getBranch
    remoteBranches.map(_.getName).find(_ == s"${RemoteBranchPrefix}${shortBranchName}") match {
      case Some(branch) => branch
      case None => throw new RuntimeException("Cannot determine currently checked out branch")
    }
  }

  private def remoteBranches = new Git(repo).branchList().setListMode(branchListMode).call().toList

}

object BranchesSelector {
  val RemoteBranchPrefix = "refs/remotes/origin/"
}
