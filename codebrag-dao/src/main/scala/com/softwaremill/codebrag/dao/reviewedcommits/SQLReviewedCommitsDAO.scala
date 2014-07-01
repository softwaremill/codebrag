package com.softwaremill.codebrag.dao.reviewedcommits

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.ReviewedCommit
import org.joda.time.{DateTime, DateTimeZone}
import com.typesafe.scalalogging.slf4j.Logging

class SQLReviewedCommitsDAO(database: SQLDatabase) extends ReviewedCommitsDAO with Logging {

  import database.driver.simple._
  import database._

  override def allReviewedByUser(userId: ObjectId, repoName: String): Set[ReviewedCommit] = {
    db.withTransaction { implicit session =>
      reviewedCommits.filter( rc => rc.userId === userId && rc.repoName === repoName).list().toSet
    }
  }

  override def storeReviewedCommit(commit: ReviewedCommit) = {
    db.withTransaction { implicit session =>
      val reviewedDateAsUTC = commit.date.withZone(DateTimeZone.UTC)
      reviewedCommits += commit.copy(date = reviewedDateAsUTC)
    }
  }

  private class ReviewedCommits(tag: Tag) extends Table[ReviewedCommit](tag, "reviewed_commits") {

    def userId = column[ObjectId]("user_id")
    def sha = column[String]("sha")
    def repoName = column[String]("repo_name")
    def reviewDate = column[DateTime]("review_date")

    def pk = primaryKey("reviewed_commits_id", (userId, sha, repoName))

    def * = (sha, userId, repoName, reviewDate) <> (ReviewedCommit.tupled, ReviewedCommit.unapply)
  }

  private val reviewedCommits = TableQuery[ReviewedCommits]

}