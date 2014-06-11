package com.softwaremill.codebrag.dao.branchsnapshot

import com.softwaremill.codebrag.domain.BranchState

trait BranchStateDAO {

  def storeBranchState(state: BranchState)

  def loadBranchState(repoName: String, branchName: String): Option[BranchState]

  def loadBranchesState(repoName: String): Set[BranchState]

  def loadBranchesStateAsMap(repoName: String): Map[String, String]

  def removeBranches(repoName: String, branches: Set[String])

}