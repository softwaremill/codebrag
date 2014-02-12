package com.softwaremill.codebrag.dao.reviewtask

import com.softwaremill.codebrag.dao.sql.{WithSQLSchemas, SQLDatabase}
import com.softwaremill.codebrag.domain.CommitReviewTask
import scala.slick.driver.JdbcProfile
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Clock

class SQLCommitReviewTaskDAO(val database: SQLDatabase, clock: Clock)
  extends CommitReviewTaskDAO with WithSQLSchemas with SQLCommitReviewTaskSchema {

  import database.driver.simple._
  import database._

  def save(toReview: CommitReviewTask) {
    db.withTransaction { implicit session =>
      val existing = Query(commitReviewTasks.filter(
        c => c.commitId === toReview.commitId && c.userId === toReview.userId).length).first()

      if (existing == 0) {
        commitReviewTasks += SQLCommitReviewTask(toReview.commitId, toReview.userId, clock.nowUtc)
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

  def schemas: Iterable[JdbcProfile#DDLInvoker] = List(commitReviewTasks.ddl)
}
