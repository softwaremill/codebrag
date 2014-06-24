package com.softwaremill.codebrag.dao.repo

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain.UserRepoDetails
import org.bson.types.ObjectId
import org.joda.time.DateTime

class SQLUserRepoDetailsDAO(database: SQLDatabase) extends UserRepoDetailsDAO {

  import database.driver.simple._
  import database._

  def save(context: UserRepoDetails) = {
    db.withTransaction { implicit session =>
      if(context.default) resetUserDefault(context.userId)
      val query = userRepoDetails.filter(c => c.userId === context.userId && c.repoName === context.repoName)
      query.firstOption match {
        case None => userRepoDetails += context
        case Some(c) => query.update(context)
      }
    }
  }

  def findDefault(userId: ObjectId): Option[UserRepoDetails] = {
    db.withTransaction {implicit session =>
      userRepoDetails.filter(c => c.userId === userId && c.default === true).firstOption
    }
  }

  def find(userId: ObjectId, repoName: String): Option[UserRepoDetails] = {
    db.withTransaction {implicit session =>
      userRepoDetails.filter(c => c.userId === userId && c.repoName === repoName).firstOption
    }
  }

  private def resetUserDefault(userId: ObjectId)(implicit session: Session) = userRepoDetails.filter(_.userId === userId).map(_.default).update(false)

  private class UserRepoDetailsTable(tag: Tag) extends Table[UserRepoDetails](tag, "user_repo_details") {

    def userId = column[ObjectId]("user_id")
    def repoName = column[String]("repo_name")
    def branchName = column[String]("branch_name")
    def toReviewSince = column[DateTime]("to_review_since")
    def default = column[Boolean]("default")

    def pk = primaryKey("user_repo_branch", (userId, repoName, branchName))
    def * = (userId, repoName, branchName, toReviewSince, default) <> (UserRepoDetails.tupled, UserRepoDetails.unapply)
  }

  private val userRepoDetails = TableQuery[UserRepoDetailsTable]

}
