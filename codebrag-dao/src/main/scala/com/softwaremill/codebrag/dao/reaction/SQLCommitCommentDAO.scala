package com.softwaremill.codebrag.dao.reaction

import com.softwaremill.codebrag.dao.sql.{SQLDatabase, WithSQLSchemas}
import com.softwaremill.codebrag.domain.{Comment, ThreadDetails}
import org.bson.types.ObjectId
import org.joda.time.DateTime
import scala.slick.driver.JdbcProfile

class SQLCommitCommentDAO(database: SQLDatabase) extends CommitCommentDAO with WithSQLSchemas {
  import database.driver.simple._
  import database._

  def save(comment: Comment) = db.withTransaction { implicit session =>
    comments += comment
  }

  def findCommentsForCommits(commitId: ObjectId*): List[Comment] = db.withTransaction { implicit session =>
    comments
      .filter(_.commitId inSet commitId.toSet)
      .sortBy(_.postingTime.asc)
      .list()
  }

  def findAllCommentsForThread(thread: ThreadDetails): List[Comment] = db.withTransaction { implicit session =>
    def positionFilter(c: Comments): Column[Option[Boolean]] = (thread.fileName, thread.lineNumber) match {
      case (Some(fileName), Some(lineNumber)) => c.fileName === thread.fileName && c.lineNumber === thread.lineNumber
      case _ => c.fileName.isNull && c.lineNumber.isNull
    }

    comments
      .filter(c => c.commitId === thread.commitId && positionFilter(c))
      .list()
  }

  private class Comments(tag: Tag) extends Table[Comment](tag, "comments") {
    def id = column[ObjectId]("id", O.PrimaryKey)
    def commitId = column[ObjectId]("commit_id")
    def authorId = column[ObjectId]("author_id")
    def postingTime = column[DateTime]("posting_time")
    def message = column[String]("message")
    def fileName = column[Option[String]]("file_name")
    def lineNumber = column[Option[Int]]("line_number")

    def * = (id, commitId, authorId, postingTime, message, fileName, lineNumber) <>
      (Comment.tupled, Comment.unapply)
  }

  private val comments = TableQuery[Comments]

  def schemas: Iterable[JdbcProfile#DDLInvoker] = List(comments.ddl)
}
