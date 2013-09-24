package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.Followup
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId
import scala.None
import com.foursquare.rogue.Query
import com.foursquare.rogue
import org.joda.time.DateTime

class MongoFollowupDAO extends FollowupDAO {


  def findById(followupId: ObjectId): Option[Followup] = {
    FollowupRecord.where(_.id eqs followupId).get() match {
      case Some(record) => Some(toFollowup(record))
      case None => None
    }
  }

  def createOrUpdateExisting(followup: Followup) = {
    val alreadyExistsQuery = FollowupRecord
      .where(_.receivingUserId eqs followup.receivingUserId)
      .and(_.threadId.subselect(_.commitId) eqs followup.reaction.commitId)
      .and(_.threadId.subselect(_.fileName) exists (followup.reaction.fileName.isDefined))
      .andOpt(followup.reaction.fileName)(_.threadId.subselect(_.fileName) eqs _)
      .and(_.threadId.subselect(_.lineNumber) exists (followup.reaction.lineNumber.isDefined))
      .andOpt(followup.reaction.lineNumber)(_.threadId.subselect(_.lineNumber) eqs _)

    val modificationQuery = buildModificationQuery(followup, alreadyExistsQuery)
    modificationQuery.updateOne(true) match {
      case Some(updated) => {
        updated.id.get
      }
      case None => {
        val incomingFollowupRecord = toRecord(followup)
        incomingFollowupRecord.save.id.get
      }
    }
  }

  def buildModificationQuery(followup: Followup, query: Query[FollowupRecord, FollowupRecord, rogue.InitialState]) = {
    query.findAndModify(_.lastReaction.subfield(_.reactionId) setTo followup.reaction.id)
      .and(_.lastReaction.subfield(_.reactionAuthorId) setTo followup.reaction.authorId)
      .and(_.lastReaction.subfield(_.reactionType) setTo LastReactionRecord.ReactionTypeEnum(followup.reaction.reactionType.id))
      .and(_.reactions push followup.reaction.id)
  }

  override def delete(followupId: ObjectId) {
    FollowupRecord.where(_.id eqs followupId).findAndDeleteOne()
  }


  override def countSince(date: DateTime, userId: ObjectId): Long = {
    FollowupRecord where (_.id after date) and (_.receivingUserId eqs userId) count()
  }

  private def toFollowup(record: FollowupRecord) = {
    // TODO: if reaction is needed, fill it using like/comment daos
    Followup(record.receivingUserId.get, null)
  }

  private def toRecord(followup: Followup): FollowupRecord = {

    val lastReactionRecord = LastReactionRecord.createRecord
      .reactionAuthorId(followup.reaction.authorId)
      .reactionId(followup.reaction.id)
      .reactionType(LastReactionRecord.ReactionTypeEnum(followup.reaction.reactionType.id))

    val threadDetailsRecord = (followup.reaction.fileName, followup.reaction.lineNumber) match {
      case (Some(fileName), Some(lineNumber)) => ThreadIdRecord.createRecord.commitId(followup.reaction.commitId).fileName(fileName).lineNumber(lineNumber)
      case _ => ThreadIdRecord.createRecord.commitId(followup.reaction.commitId)
    }

    FollowupRecord.createRecord
      .threadId(threadDetailsRecord)
      .lastReaction(lastReactionRecord)
      .receivingUserId(followup.receivingUserId)
      .reactions(List(followup.reaction.id))
  }

}