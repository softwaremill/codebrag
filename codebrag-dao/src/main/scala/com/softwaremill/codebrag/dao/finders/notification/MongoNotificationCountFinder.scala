package com.softwaremill.codebrag.dao.finders.notification

import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskRecord
import com.softwaremill.codebrag.dao.followup.FollowupRecord
import com.softwaremill.codebrag.dao.finders.views.NotificationCountersView


class MongoNotificationCountFinder extends NotificationCountFinder {

  def getCounters(userId: ObjectId): NotificationCountersView = {
    val followupCount = FollowupRecord.where(_.receivingUserId eqs userId).count()
    val commitCount = CommitReviewTaskRecord.where(_.userId eqs userId).count()
    NotificationCountersView(commitCount, followupCount)
  }

  def getCountersSince(date: DateTime, userId: ObjectId): NotificationCountersView = {
    val followupCount = FollowupRecord.where(_.receivingUserId eqs userId).and(_.lastReaction.subselect(_.reactionId) after date).count()
    val commitCount = CommitReviewTaskRecord.where(_.id after date).and(_.userId eqs userId).count()
    NotificationCountersView(commitCount, followupCount)
  }
}
