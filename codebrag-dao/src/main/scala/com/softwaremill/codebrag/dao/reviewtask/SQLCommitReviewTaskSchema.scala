package com.softwaremill.codebrag.dao.reviewtask

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain.CommitReviewTask
import org.bson.types.ObjectId
import org.joda.time.DateTime

trait SQLCommitReviewTaskSchema {
  val database: SQLDatabase

  import database.driver.simple._
  import database._

  protected case class SQLCommitReviewTask(commitId: ObjectId, userId: ObjectId, createdDate: DateTime) {
    def toCommitReviewTask = CommitReviewTask(commitId, userId)
  }

  protected class CommitReviewTasks(tag: Tag) extends Table[SQLCommitReviewTask](tag, "commit_review_tasks") {
    def commitId    = column[ObjectId]("commit_id")
    def userId      = column[ObjectId]("user_id")
    def createdDate = column[DateTime]("created_date")

    def pk = primaryKey("commit_review_tasks_id", (commitId, userId))

    def * = (commitId, userId, createdDate) <> (SQLCommitReviewTask.tupled, SQLCommitReviewTask.unapply)
  }

  protected val commitReviewTasks = TableQuery[CommitReviewTasks]
}
