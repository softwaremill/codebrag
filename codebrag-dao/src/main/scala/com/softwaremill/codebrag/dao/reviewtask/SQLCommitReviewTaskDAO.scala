package com.softwaremill.codebrag.dao.reviewtask

import com.softwaremill.codebrag.dao.sql.{WithSQLSchemas, SQLDatabase}
import com.softwaremill.codebrag.domain.CommitReviewTask
import scala.slick.driver.JdbcProfile
import org.bson.types.ObjectId

class SQLCommitReviewTaskDAO(database: SQLDatabase) extends CommitReviewTaskDAO with WithSQLSchemas {
  import database.driver.simple._
  import database._

  def save(toReview: CommitReviewTask) {
    db.withTransaction { implicit session =>
      val existing = Query(commitReviewTasks.filter(
        c => c.commitId === toReview.commitId && c.userId === toReview.userId).length).first()

      if (existing == 0) {
        commitReviewTasks += toReview
      }
    }
  }

  def delete(task: CommitReviewTask) {
    db.withTransaction { implicit session =>
      commitReviewTasks.filter(c => c.commitId === task.commitId && c.userId === task.userId).delete
    }
  }

  def commitsPendingReviewFor(userId: ObjectId) = db.withTransaction { implicit session =>
    commitReviewTasks.filter(c => c.userId === userId).map(_.commitId).list().toSet
  }

  private class CommitReviewTasks(tag: Tag) extends Table[CommitReviewTask](tag, "commit_review_tasks") {
    def commitId  = column[ObjectId]("commit_id")
    def userId    = column[ObjectId]("user_id")

    def pk = primaryKey("commit_review_tasks_id", (commitId, userId))

    def * = (commitId, userId) <> (CommitReviewTask.tupled, CommitReviewTask.unapply)
  }

  private val commitReviewTasks = TableQuery[CommitReviewTasks]

  def schemas: Iterable[JdbcProfile#DDLInvoker] = List(commitReviewTasks.ddl)
}
