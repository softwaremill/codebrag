package com.softwaremill.codebrag.dao.branch

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain.UserWatchedBranch
import org.bson.types.ObjectId

class SQLWatchedBranchesDao(database: SQLDatabase) extends WatchedBranchesDao {

  import database.driver.simple._
  import database._

  override def save(branch: UserWatchedBranch) = db.withTransaction { implicit session =>
      userObservedBranches += toSQLObservedBranch(branch)
  }

  override def delete(id: ObjectId) = db.withTransaction { implicit session =>
    userObservedBranches.where(_.id === id).delete
  }

  def findAll(userId: ObjectId): Set[UserWatchedBranch] = db.withTransaction { implicit session =>
    userObservedBranches.filter(_.userId === userId).list().map(_.toUserObservedBranch).toSet
  }

  private class UserObservedBranchTable(tag: Tag) extends Table[SQLUserObservedBranch](tag, "watched_branches") {

    def id = column[ObjectId]("id", O.PrimaryKey)
    def userId = column[ObjectId]("user_id")
    def repoName = column[String]("repo_name")
    def branchName = column[String]("branch_name")

    def pk = primaryKey("user_repo_branch", (userId, repoName, branchName))
    def * = (id, userId, repoName, branchName) <> (SQLUserObservedBranch.tupled, SQLUserObservedBranch.unapply)
  }

  private val userObservedBranches = TableQuery[UserObservedBranchTable]

  protected case class SQLUserObservedBranch(id: ObjectId, userId: ObjectId, repoName: String, branchName: String) {
    def toUserObservedBranch = UserWatchedBranch(id, userId, repoName, branchName)
  }

  private def toSQLObservedBranch(branch: UserWatchedBranch) = SQLUserObservedBranch(branch.id, branch.userId, branch.repoName, branch.branchName)

}
