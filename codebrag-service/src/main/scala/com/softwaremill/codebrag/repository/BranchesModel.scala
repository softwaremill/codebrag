package com.softwaremill.codebrag.repository

import org.eclipse.jgit.api.Git
import scala.collection.JavaConversions._
import org.eclipse.jgit.api.ListBranchCommand.ListMode

trait BranchesModel {

  def remoteBranchesFullNames: Set[String]

  def findStaleBranchesFullNames(locallyCachedBranches: Set[String]): Set[String]

  def getCheckedOutBranchFullName: String

  def resolveFullBranchName(branchName: String) = {
    if(branchName.startsWith(RepositoryBranchPrefix)) {
      branchName
    } else {
      s"${RepositoryBranchPrefix}${branchName}"
    }
  }

  val RepositoryBranchPrefix: String

}

trait GitRepoBranchesModel extends BranchesModel {

  self: GitRepository =>

  def remoteBranchesFullNames = {
    remoteBranches.map(_.getName).filterNot(_ == s"${RepositoryBranchPrefix}HEAD").toSet
  }

  def findStaleBranchesFullNames(locallyCachedBranches: Set[String]) = {
    val branches = remoteBranchesFullNames
    locallyCachedBranches.filterNot(branches.contains)
  }

  def getCheckedOutBranchFullName = s"${RepositoryBranchPrefix}${repo.getBranch}"

  val RepositoryBranchPrefix = "refs/remotes/origin/"

  val BranchListMode = ListMode.REMOTE

  private def remoteBranches = new Git(repo).branchList().setListMode(BranchListMode).call().toList

}

trait GitSvnBranchesModel extends BranchesModel {

  self: GitSvnRepository =>

  def remoteBranchesFullNames = Set("master")

  def findStaleBranchesFullNames(locallyCachedBranches: Set[String]) = Set.empty

  def getCheckedOutBranchFullName = "master"

  val RepositoryBranchPrefix = ""

}