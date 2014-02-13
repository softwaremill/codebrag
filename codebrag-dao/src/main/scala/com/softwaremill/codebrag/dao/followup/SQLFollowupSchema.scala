package com.softwaremill.codebrag.dao.followup

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.bson.types.ObjectId
import scala.slick.model.ForeignKeyAction
import org.joda.time.DateTime

trait SQLFollowupSchema {
  val database: SQLDatabase

  import database.driver.simple._
  import database._

  protected case class SQLFollowup(
    id: ObjectId,
    receivingUserId: ObjectId,
    threadCommitId: ObjectId,
    threadFileName: Option[String],
    threadLineNumber: Option[Int],
    lastReactionId: ObjectId,
    lastReactionCreatedDate: DateTime,
    lastReactionAuthor: ObjectId)

  protected class Followups(tag: Tag) extends Table[SQLFollowup](tag, "followups") {
    def id                      = column[ObjectId]("id", O.PrimaryKey)
    def receivingUserId         = column[ObjectId]("receiving_user_id")
    def threadCommitId          = column[ObjectId]("thread_commit_id")
    def threadFileName          = column[Option[String]]("thread_file_name")
    def threadLineNumber        = column[Option[Int]]("thread_line_number")
    def lastReactionId          = column[ObjectId]("last_reaction_id")
    def lastReactionCreatedDate = column[DateTime]("last_reaction_created_date")
    def lastReactionAuthor      = column[ObjectId]("last_reaction_author")

    def * = (id, receivingUserId, threadCommitId, threadFileName, threadLineNumber, lastReactionId, lastReactionCreatedDate, lastReactionAuthor) <>
      (SQLFollowup.tupled, SQLFollowup.unapply)
  }

  protected val followups = TableQuery[Followups]

  protected class FollowupsReactions(tag: Tag) extends Table[(ObjectId, ObjectId)](tag, "followups_reactions") {
    def followupId = column[ObjectId]("followup_id")
    def reactionId = column[ObjectId]("reaction_id")

    def pk = primaryKey("followup_reactions_pk", (followupId, reactionId))
    def followup = foreignKey("followup_reactions_fk", followupId, followups)(_.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)

    def * = (followupId, reactionId)
  }

  protected val followupsReactions = TableQuery[FollowupsReactions]
}
