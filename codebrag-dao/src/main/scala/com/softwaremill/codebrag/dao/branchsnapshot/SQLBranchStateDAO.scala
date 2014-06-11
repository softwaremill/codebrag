package com.softwaremill.codebrag.dao.branchsnapshot

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain.BranchState

class SQLBranchStateDAO(database: SQLDatabase) extends BranchStateDAO {

  import database.driver.simple._
  import database._

  def storeBranchState(state: BranchState) = {
    db.withTransaction { implicit session =>
      branchStates.filter(_.repoName === state.repoName).filter(_.branchName === state.fullBranchName).firstOption match {
        case None => branchStates += state
        case Some(existing) => branchStates.where( row =>
          (row.repoName === state.repoName) && (row.branchName === state.fullBranchName)
        ).update(state)
      }
    }
  }

  def loadBranchState(repoName: String, branchName: String): Option[BranchState] = {
    db.withTransaction { implicit session =>
      branchStates.where( row =>
        (row.repoName === repoName) && (row.branchName === branchName)
      ).firstOption
    }
  }

  def loadBranchesState(repoName: String): Set[BranchState] = {
    db.withTransaction { implicit session =>
      branchStates.where(_.repoName === repoName).list().toSet
    }
  }

  def loadBranchesStateAsMap(repoName: String) = loadBranchesState(repoName).map(b => (b.fullBranchName, b.sha)).toMap

  override def removeBranches(repoName: String, branches: Set[String]) {
    db.withTransaction { implicit session =>
      branchStates.where( row =>
        (row.repoName === repoName) && (row.branchName inSet branches)
      ).delete
    }
  }

  private class BranchStates(tag: Tag) extends Table[BranchState](tag, "branch_states") {

    def branchName = column[String]("branch_name", O.PrimaryKey)
    def sha = column[String]("sha")
    def repoName = column[String]("repo_name")

    def * = (repoName, branchName, sha) <> (BranchState.tupled, BranchState.unapply)
  }

  private val branchStates = TableQuery[BranchStates]

}
