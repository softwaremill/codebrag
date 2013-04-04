package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.FollowupRecord
import com.foursquare.rogue.LiftRogue._

class MongoFollowupFinder extends FollowupFinder {

  def findAllFollowupsForUser(userId: ObjectId): FollowupsList = {
    val followupRecords = FollowupRecord.where(_.user_id eqs userId).orderDesc(_.date).fetch()
    FollowupsList(followupRecords.map(toFollowupInfo(_)))
  }

  def toFollowupInfo(record: FollowupRecord) = {
    val commitInfo: FollowupCommitInfo = extractCommitInfoFromRecord(record)
    SingleFollowupInfo(record.user_id.get.toString, record.date.get, commitInfo)
  }

  def extractCommitInfoFromRecord(record: FollowupRecord) = {
    val commit = record.commit.get
    FollowupCommitInfo(commit.id.get.toString, commit.author.get, commit.message.get, commit.date.get)
  }

}
