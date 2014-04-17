package com.softwaremill.codebrag.dao.branchsnapshot

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain.BranchState

class SQLBranchStateDAO(database: SQLDatabase) extends BranchStateDAO {

  import database.driver.simple._
  import database._

  def storeBranchState(state: BranchState) = {
    db.withTransaction { implicit session =>
      branchStates.filter(_.branchName === state.fullBranchName).firstOption match {
        case None => branchStates += state
        case Some(existing) => branchStates
          .filter(_.branchName === state.fullBranchName)
          .update(state)
      }
    }
  }

  def loadBranchState(branchName: String): Option[BranchState] = {
    db.withTransaction { implicit session =>
      branchStates.filter(_.branchName === branchName).firstOption
    }
  }

  def loadBranchesState: Set[BranchState] = {
    db.withTransaction { implicit session =>
      branchStates.list().toSet
    }
  }

  def loadBranchesStateAsMap = loadBranchesState.map(b => (b.fullBranchName, b.sha)).toMap

  override def removeBranches(branches: Set[String]) {
    db.withTransaction { implicit session =>
      branchStates.filter(_.branchName inSet branches).delete
    }
  }

  private class BranchStates(tag: Tag) extends Table[BranchState](tag, "branch_states") {

    def branchName = column[String]("branch_name", O.PrimaryKey)
    def sha = column[String]("sha")

    def * = (branchName, sha) <> (BranchState.tupled, BranchState.unapply)
  }

  private val branchStates = TableQuery[BranchStates]

}
