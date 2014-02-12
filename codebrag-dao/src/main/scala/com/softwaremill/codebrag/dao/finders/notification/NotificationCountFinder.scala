package com.softwaremill.codebrag.dao.finders.notification

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView
import org.joda.time.DateTime

trait NotificationCountFinder {
  def getCounters(userId: ObjectId): NotificationCountersView

  def getCountersSince(date: DateTime, userId: ObjectId): NotificationCountersView
}
