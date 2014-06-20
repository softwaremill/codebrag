package com.softwaremill.codebrag.dao.browsingcontext

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain.UserBrowsingContext
import org.bson.types.ObjectId

class SQLUserBrowsingContextDAO(database: SQLDatabase) extends UserBrowsingContextDAO {

  import database.driver.simple._
  import database._

  def save(context: UserBrowsingContext) = {
    db.withTransaction { implicit session =>
      if(context.default) resetUserDefault(context.userId)
      val query = userBrowsingContexts.filter(c => c.userId === context.userId && c.repoName === context.repoName)
      query.firstOption match {
        case None => userBrowsingContexts += context
        case Some(c) => query.update(context)
      }
    }
  }

  def findDefault(userId: ObjectId): Option[UserBrowsingContext] = {
    db.withTransaction {implicit session =>
      userBrowsingContexts.filter(c => c.userId === userId && c.default === true).firstOption
    }
  }

  def find(userId: ObjectId, repoName: String): Option[UserBrowsingContext] = {
    db.withTransaction {implicit session =>
      userBrowsingContexts.filter(c => c.userId === userId && c.repoName === repoName).firstOption
    }
  }

  private def resetUserDefault(userId: ObjectId)(implicit session: Session) = userBrowsingContexts.filter(_.userId === userId).map(_.default).update(false)

  private class UserBrowsingContexts(tag: Tag) extends Table[UserBrowsingContext](tag, "user_browsing_contexts") {

    def userId = column[ObjectId]("user_id")
    def repoName = column[String]("repo_name")
    def branchName = column[String]("branch_name")
    def default = column[Boolean]("default")

    def pk = primaryKey("user_repo_branch", (userId, repoName, branchName))
    def * = (userId, repoName, branchName, default) <> (UserBrowsingContext.tupled, UserBrowsingContext.unapply)
  }

  private val userBrowsingContexts = TableQuery[UserBrowsingContexts]

}
