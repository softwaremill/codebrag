package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.{UserRecord, FollowupRecord}
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.dao.reporting.views.{FollowupReactionView, FollowupView, FollowupCommitView, FollowupListView}

class MongoFollowupFinder extends FollowupFinder {

  def findAllFollowupsForUser(userId: ObjectId): FollowupListView = {
    val followupRecords = FollowupRecord.where(_.user_id eqs userId).orderDesc(_.date).fetch()
    val userAvatarMap = UserRecord.select(_.id, _.avatarUrl).where(_.id in followupRecords.map(_.author_id.get)).fetch().toMap

    val followups = followupRecords.map(record => {
      val avatarOption = userAvatarMap.get(record.author_id.get)
      toFollowupInfo(record, avatarOption)
    })

    FollowupListView(followups)
  }

  def findFollowupForUser(userId: ObjectId, followupId: ObjectId) = {
    val recordOpt = FollowupRecord.where(_.user_id eqs userId).and(_.followupId eqs followupId).get()

    val avatarOpt = recordOpt.flatMap(record => {
    getAvatarForUser(record.author_id.get)
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
    FollowupReactionView(record.reactionId.get.toString, record.lastCommenterName.get, avatarUrl)
  }

  def toFollowupInfo(record: FollowupRecord, avatarUrl: Option[String]) = {
    val commitInfo = extractCommitInfoFromRecord(record)
    val reactionInfo = extractReactionInfoFromRecord(record, avatarUrl.filter(_.nonEmpty))
    FollowupView(record.followupId.get.toString, record.date.get, commitInfo, reactionInfo)
  }

  def extractCommitInfoFromRecord(record: FollowupRecord) = {
    val commit = record.commit.get
    FollowupCommitView(commit.id.get.toString, commit.author.get, commit.message.get, commit.date.get)
  }

}
