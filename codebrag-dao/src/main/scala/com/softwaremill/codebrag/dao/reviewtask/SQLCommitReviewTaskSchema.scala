package com.softwaremill.codebrag.dao.reviewtask

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.domain.CommitReviewTask
import org.bson.types.ObjectId

trait SQLCommitReviewTaskSchema {
  val database: SQLDatabase

  import database.driver.simple._
  import database._

  protected class CommitReviewTasks(tag: Tag) extends Table[CommitReviewTask](tag, "commit_review_tasks") {
    def commitId  = column[ObjectId]("commit_id")
    def userId    = column[ObjectId]("user_id")

    def pk = primaryKey("commit_review_tasks_id", (commitId, userId))

    def * = (commitId, userId) <> (CommitReviewTask.tupled, CommitReviewTask.unapply)
  }

  protected val commitReviewTasks = TableQuery[CommitReviewTasks]
}
