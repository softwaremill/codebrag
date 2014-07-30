package com.softwaremill.codebrag.dao.observedbranch

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain.UserObservedBranch
import org.bson.types.ObjectId

class SQLUserObservedBranchDAO(database: SQLDatabase) extends UserObservedBranchDAO {

  import database.driver.simple._
  import database._

  override def save(branch: UserObservedBranch) = db.withTransaction { implicit session =>
      userObservedBranches += toSQLObservedBranch(branch)
  }

  override def delete(id: ObjectId) = db.withTransaction { implicit session =>
    userObservedBranches.where(_.id === id).delete
  }

  def findAll(userId: ObjectId): Set[UserObservedBranch] = db.withTransaction { implicit session =>
    userObservedBranches.filter(_.userId === userId).list().map(_.toUserObservedBranch).toSet
  }

  private class UserObservedBranchTable(tag: Tag) extends Table[SQLUserObservedBranch](tag, "user_observed_branches") {

    def id = column[ObjectId]("id", O.PrimaryKey)
    def userId = column[ObjectId]("user_id")
    def repoName = column[String]("repo_name")
    def branchName = column[String]("branch_name")

    def pk = primaryKey("user_repo_branch", (userId, repoName, branchName))
    def * = (id, userId, repoName, branchName) <> (SQLUserObservedBranch.tupled, SQLUserObservedBranch.unapply)
  }

  private val userObservedBranches = TableQuery[UserObservedBranchTable]

  protected case class SQLUserObservedBranch(id: ObjectId, userId: ObjectId, repoName: String, branchName: String) {
    def toUserObservedBranch = UserObservedBranch(id, userId, repoName, branchName)
  }

  private def toSQLObservedBranch(branch: UserObservedBranch) = SQLUserObservedBranch(branch.id, branch.userId, branch.repoName, branch.branchName)

}
