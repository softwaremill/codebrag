package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.{UserRecord, FollowupRecord}
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.dao.reporting.views.{FollowupReactionView, FollowupView, FollowupCommitView, FollowupListView}

class MongoFollowupFinder extends FollowupFinder {

  def findAllFollowupsForUser(userId: ObjectId): FollowupListView = {
    val followupRecords = FollowupRecord.where(_.receivingUserId eqs userId).orderDesc(_.lastReaction.subfield(_.date)).fetch()
    val authorIds = followupRecords.map(r => r.lastReaction.get.authorId.get)
    val userAvatarMap = UserRecord.select(_.id, _.avatarUrl).where(_.id in authorIds).fetch().toMap

    val followups = followupRecords.map(record => {
      val avatarOption = userAvatarMap.get(record.lastReaction.get.authorId.get)
      toFollowupInfo(record, avatarOption)
    })

    FollowupListView(followups)
  }

  def findFollowupForUser(userId: ObjectId, followupId: ObjectId) = {
    val recordOpt = FollowupRecord.where(_.receivingUserId eqs userId).and(_.id eqs followupId).get()

    val avatarOpt = recordOpt.flatMap(record => {
    getAvatarForUser(record.lastReaction.get.authorId.get)
    })

    recordOpt match {
      case Some(record) => Right(toFollowupInfo(record, avatarOpt))
      case None => Left("No such followup")
    }
  }

  private def getAvatarForUser(userId: ObjectId): Option[String] = {
    UserRecord.select(_.avatarUrl).where(_.id eqs userId).get()
  }

  def extractReactionInfoFromRecord(record: FollowupRecord, avatarUrl: Option[String]) = {
    FollowupReactionView(record.lastReaction.get.reactionId.get.toString, record.lastReaction.get.authorName.get, avatarUrl)
  }

  def toFollowupInfo(record: FollowupRecord, avatarUrl: Option[String]) = {
    val commitInfo = extractCommitInfoFromRecord(record)
    val reactionInfo = extractReactionInfoFromRecord(record, avatarUrl.filter(_.nonEmpty))
    FollowupView(record.id.get.toString, record.lastReaction.get.date.get, commitInfo, reactionInfo)
  }

  def extractCommitInfoFromRecord(record: FollowupRecord) = {
    val commit = record.commit.get
    FollowupCommitView(commit.id.get.toString, commit.author.get, commit.message.get, commit.date.get)
  }

}
