package com.softwaremill.codebrag.dao.branchsnapshot

import com.softwaremill.codebrag.domain.BranchState

trait BranchStateDAO {

  def storeBranchState(state: BranchState)

  def loadBranchState(branchName: String): Option[BranchState]

  def loadBranchesState: Set[BranchState]

  def loadBranchesStateAsMap: Map[String, String]

  def removeBranches(branches: Set[String])

}

class InMemoryBranchStateDAO extends BranchStateDAO {

  private val storage = new scala.collection.mutable.HashSet[BranchState]

  def storeBranchState(state: BranchState) {
    storage.add(state)
  }

  def loadBranchState(branchName: String) = {
    storage.find(_.fullBranchName == branchName)
  }

  def loadBranchesState = storage.toSet

  def loadBranchesStateAsMap =  loadBranchesState.map( b => (b.fullBranchName, b.sha)).toMap

  def removeBranches(branches: Set[String]) {
    val toRemove = storage.filter(b => branches.contains(b.fullBranchName))
    toRemove.foreach(storage.remove)
  }
}
