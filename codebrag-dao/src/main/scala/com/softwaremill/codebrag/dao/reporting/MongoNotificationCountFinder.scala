package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView
import com.softwaremill.codebrag.dao.{CommitReviewTaskRecord, FollowupRecord}
import com.foursquare.rogue.LiftRogue._


class MongoNotificationCountFinder extends NotificationCountFinder {

  def getCounters(userId: ObjectId): NotificationCountersView = {
    val followupCount = FollowupRecord.where(_.user_id eqs userId).count()
    val commitCount = CommitReviewTaskRecord.where(_.userId eqs userId).count()
    NotificationCountersView(commitCount, followupCount)
  }
}
