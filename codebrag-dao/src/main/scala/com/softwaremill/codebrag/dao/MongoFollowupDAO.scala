package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{ThreadDetails, Followup}
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId
import org.joda.time.DateTime
import scala.None
import com.foursquare.rogue.Query
import com.foursquare.rogue

class MongoFollowupDAO extends FollowupDAO {


  def findById(followupId: ObjectId): Option[Followup] = {
    FollowupRecord.where(_.id eqs followupId).get() match {
      case Some(record) => Some(toFollowup(record))
      case None => None
    }
  }

  def createOrUpdateExisting(followup: Followup) = {
    val alreadyExistsQuery = FollowupRecord
      .where(_.receivingUserId eqs followup.userId)
      .and(_.threadId.subselect(_.commitId) eqs followup.threadId.commitId)
      .and(_.threadId.subselect(_.fileName) exists(followup.threadId.fileName.isDefined))
      .andOpt(followup.threadId.fileName)(_.threadId.subselect(_.fileName) eqs _)
      .and(_.threadId.subselect(_.lineNumber) exists(followup.threadId.lineNumber.isDefined))
      .andOpt(followup.threadId.lineNumber)(_.threadId.subselect(_.lineNumber) eqs _)

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
    query.findAndModify(_.lastReaction.subfield(_.authorId) setTo followup.authorId)
      .and(_.lastReaction.subfield(_.reactionType) setTo LastReactionRecord.ReactionTypeEnum(followup.followupType.id))
      .and(_.lastReaction.subfield(_.authorName) setTo followup.lastCommenterName)
      .and(_.lastReaction.subfield(_.reactionId) setTo followup.reactionId)
      .and(_.lastReaction.subfield(_.date) setTo followup.date)
      .and(_.reactions push followup.reactionId)
  }

  override def delete(followupId: ObjectId) {
    FollowupRecord.where(_.id eqs followupId).findAndDeleteOne()
  }

  private def toFollowup(record: FollowupRecord) = {
    val threadId = ThreadDetails(record.threadId.get.commitId.get, record.threadId.get.lineNumber.get, record.threadId.get.fileName.get)
    val followupType = Followup.FollowupType.apply(record.lastReaction.get.reactionType.get.id)
    Followup(
      record.lastReaction.get.reactionId.get,
      record.lastReaction.get.authorId.get,
      record.receivingUserId.get,
      new DateTime(record.lastReaction.get.date.get),
      record.lastReaction.get.authorName.get,
      threadId,
      followupType)
  }

  private def toRecord(followup: Followup): FollowupRecord = {

    val commit = CommitInfoRecord.where(_.id eqs followup.threadId.commitId).get().getOrElse(
      throw new IllegalStateException(s"Cannot find commit ${followup.threadId.commitId}")
    )

    val commitRecord = FollowupCommitInfoRecord.createRecord
      .id(followup.threadId.commitId)
      .message(commit.message.get)
      .author(commit.authorName.get)
      .date(commit.committerDate.get)

    val lastReactionRecord = LastReactionRecord.createRecord
      .authorId(followup.authorId)
      .authorName(followup.lastCommenterName)
      .date(followup.date.toDate)
      .reactionId(followup.reactionId)
      .reactionType(LastReactionRecord.ReactionTypeEnum(followup.followupType.id))

    val threadDetailsRecord = (followup.threadId.fileName, followup.threadId.lineNumber) match {
      case (Some(fileName), Some(lineNumber)) => ThreadIdRecord.createRecord.commitId(followup.threadId.commitId).fileName(fileName).lineNumber(lineNumber)
      case _ => ThreadIdRecord.createRecord.commitId(followup.threadId.commitId)
    }

    FollowupRecord.createRecord
      .commit(commitRecord)
      .threadId(threadDetailsRecord)
      .lastReaction(lastReactionRecord)
      .receivingUserId(followup.userId)
      .reactions(List(followup.reactionId))
  }

}