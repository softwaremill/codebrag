package com.softwaremill.codebrag.dao.finders.notification

import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.followup.SQLFollowupSchema
import com.softwaremill.codebrag.dao.reviewtask.SQLCommitReviewTaskSchema
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView

class SQLNotificationCountFinder(val database: SQLDatabase)
  extends NotificationCountFinder with SQLCommitReviewTaskSchema with SQLFollowupSchema {

  import database.driver.simple._
  import database._

  def getCounters(userId: ObjectId) = db.withTransaction { implicit session =>
    val followupCount = Query(followups.where(_.receivingUserId === userId).length).first()
    val commitCount = Query(commitReviewTasks.filter(_.userId === userId).length).first()
    NotificationCountersView(commitCount, followupCount)
  }

  def getCountersSince(date: DateTime, userId: ObjectId) = db.withTransaction { implicit session =>
    val followupCount = Query(followups.where(f => f.receivingUserId === userId && f.lastReactionCreatedDate > date).length).first()
    val commitCount = Query(commitReviewTasks.filter(c => c.userId === userId && c.createdDate > date).length).first()
    NotificationCountersView(commitCount, followupCount)
  }
}
