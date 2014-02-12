package com.softwaremill.codebrag.dao.followup

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.Followup

class SQLFollowupDAO(val database: SQLDatabase) extends FollowupDAO with SQLFollowupSchema {
  import database.driver.simple._
  import database._

  def findReceivingUserId(followupId: ObjectId) = db.withTransaction { implicit session =>
    followups.filter(_.id === followupId).map(_.receivingUserId).firstOption
  }

  def createOrUpdateExisting(followup: Followup) = db.withTransaction { implicit session =>
    def fileNameCondition(f: Followups): Column[Option[Boolean]] = followup.reaction.fileName match {
      case Some(fileName) => f.threadFileName === fileName
      case None => f.threadFileName.isNull
    }

    def lineNumberCondition(f: Followups): Column[Option[Boolean]] = followup.reaction.lineNumber match {
      case Some(lineNumber) => f.threadLineNumber === lineNumber
      case None => f.threadLineNumber.isNull
    }

    def existingFollowupFilter(f: Followups) = f.receivingUserId === followup.receivingUserId &&
      f.threadCommitId === followup.reaction.commitId &&
      fileNameCondition(f) &&
      lineNumberCondition(f)

    val existingOpt = followups.filter(existingFollowupFilter).firstOption()

    val followupId = existingOpt match {
      case Some(existing) => {
        followups
          .filter(existingFollowupFilter)
          .map(f => (f.lastReactionId, f.lastReactionAuthor))
          .update(followup.reaction.id, followup.reaction.authorId)

        existing.id
      }
      case None => {
        val id = new ObjectId()
        followups += SQLFollowup(
          id,
          followup.receivingUserId,
          followup.reaction.commitId,
          followup.reaction.fileName,
          followup.reaction.lineNumber,
          followup.reaction.id,
          followup.reaction.authorId
        )

        id
      }
    }

    followupsReactions += (followupId, followup.reaction.id)

    followupId
  }

  def delete(followupId: ObjectId) {
    db.withTransaction { implicit session =>
      followups.filter(_.id === followupId).delete
    }
  }
}
