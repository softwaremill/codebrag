package com.softwaremill.codebrag.dao.branchsnapshot

import com.softwaremill.codebrag.domain.BranchState

trait BranchStateDAO {

  def storeBranchState(state: BranchState)

  def loadBranchState(branchName: String): Option[BranchState]

  def loadBranchesState: Set[BranchState]

  def loadBranchesStateAsMap: Map[String, String]

  def removeBranches(branches: Set[String])

}