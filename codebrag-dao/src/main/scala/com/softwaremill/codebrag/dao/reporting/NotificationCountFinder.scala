package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView

trait NotificationCountFinder {
  def getCounters(userId: ObjectId): NotificationCountersView
}
