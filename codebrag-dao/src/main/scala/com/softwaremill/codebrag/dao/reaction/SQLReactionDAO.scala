package com.softwaremill.codebrag.dao.reaction

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.bson.types.ObjectId
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.ThreadDetails

trait SQLReactionDAO {
  val database: SQLDatabase

  import database.driver.simple._
  import database._

  private[reaction] trait ReactionTable {
    this: Table[_] =>

    def id = column[ObjectId]("id", O.PrimaryKey)
    def commitId = column[ObjectId]("commit_id")
    def authorId = column[ObjectId]("author_id")
    def postingTime = column[DateTime]("posting_time")
    def fileName = column[Option[String]]("file_name")
    def lineNumber = column[Option[Int]]("line_number")
  }

  private[reaction] def positionFilter(thread: ThreadDetails, r: ReactionTable): Column[Option[Boolean]] =
    (thread.fileName, thread.lineNumber) match {
      case (Some(fileName), Some(lineNumber)) => r.fileName === thread.fileName && r.lineNumber === thread.lineNumber
      case _ => r.fileName.isNull && r.lineNumber.isNull
    }
}
