package com.softwaremill.codebrag.dao.reviewedcommits

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.ReviewedCommit
import org.joda.time.{DateTime, DateTimeZone}
import com.typesafe.scalalogging.slf4j.Logging
import org.h2.jdbc.JdbcSQLException

class SQLReviewedCommitsDAO(database: SQLDatabase) extends ReviewedCommitsDAO with Logging {

  import database.driver.simple._
  import database._

  override def allReviewedByUser(userId: ObjectId): Set[ReviewedCommit] = {
    db.withTransaction { implicit session =>
      reviewedCommits.filter(_.userId === userId).list().toSet
    }
  }

  override def storeReviewedCommit(commit: ReviewedCommit) = {
    try {
      db.withTransaction { implicit session =>
        val reviewedDateAsUTC = commit.date.withZone(DateTimeZone.UTC)
        reviewedCommits += commit.copy(date = reviewedDateAsUTC)
      }
    } catch {
      case e: JdbcSQLException => logger.error(s"User has this commit already reviewed ${commit}", e)
    }
  }

  private class ReviewedCommits(tag: Tag) extends Table[ReviewedCommit](tag, "reviewed_commits") {

    def userId = column[ObjectId]("user_id")
    def sha = column[String]("sha")
    def reviewDate = column[DateTime]("review_date")

    def pk = primaryKey("reviewed_commits_id", (userId, sha))

    def * = (sha, userId, reviewDate) <> (ReviewedCommit.tupled, ReviewedCommit.unapply)
  }

  private val reviewedCommits = TableQuery[ReviewedCommits]

}