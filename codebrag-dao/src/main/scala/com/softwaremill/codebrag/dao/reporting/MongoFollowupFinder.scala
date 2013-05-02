package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.FollowupRecord
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.dao.reporting.views.{SingleFollowupView, FollowupCommitView, FollowupListView}

class MongoFollowupFinder extends FollowupFinder {

  def findAllFollowupsForUser(userId: ObjectId): FollowupListView = {
    val followupRecords = FollowupRecord.where(_.user_id eqs userId).orderDesc(_.date).fetch()
    FollowupListView(followupRecords.map(toFollowupInfo(_)))
  }

  def toFollowupInfo(record: FollowupRecord) = {
    val commitInfo: FollowupCommitView = extractCommitInfoFromRecord(record)
    SingleFollowupView(record.followupId.get.toString, record.user_id.get.toString, record.date.get, commitInfo)
  }

  def extractCommitInfoFromRecord(record: FollowupRecord) = {
    val commit = record.commit.get
    FollowupCommitView(commit.id.get.toString, commit.author.get, commit.message.get, commit.date.get)
  }

}
