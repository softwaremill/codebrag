package com.softwaremill.codebrag.dao.finders.notification

import org.bson.types.ObjectId
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.finders.views.NotificationCountersView

trait NotificationCountFinder {
  def getCounters(userId: ObjectId): NotificationCountersView

  def getCountersSince(date: DateTime, userId: ObjectId): NotificationCountersView
}
