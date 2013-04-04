package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.FollowUpRecord
import com.foursquare.rogue.LiftRogue._

class MongoFollowUpFinder extends FollowUpFinder {

  def findAllFollowUpsForUser(userId: ObjectId): FollowUpsList = {
    val followUpRecords = FollowUpRecord.where(_.user_id eqs userId).orderDesc(_.date).fetch()
    FollowUpsList(followUpRecords.map(toFollowUpInfo(_)))
  }

  def toFollowUpInfo(record: FollowUpRecord) = {
    val commitInfo: FollowUpCommitInfo = extractCommitInfoFromRecord(record)
    SingleFollowUpInfo(record.user_id.get.toString, record.date.get, commitInfo)
  }

  def extractCommitInfoFromRecord(record: FollowUpRecord) = {
    val commit = record.commit.get
    FollowUpCommitInfo(commit.id.get.toString, commit.author.get, commit.message.get, commit.date.get)
  }

}
