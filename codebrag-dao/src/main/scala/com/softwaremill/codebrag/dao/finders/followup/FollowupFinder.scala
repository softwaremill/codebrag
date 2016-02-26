package com.softwaremill.codebrag.dao.finders.followup

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.views.{FollowupsByCommitListView, SingleFollowupView}
import org.joda.time.DateTime

trait FollowupFinder {

  def findAllFollowupsByCommitForUser(userId: ObjectId): FollowupsByCommitListView

  def findFollowupForUser(userId: ObjectId, followupId: ObjectId): Either[String, SingleFollowupView]
  
  def findFollowupforDashboard(followupId: ObjectId): Either[String, SingleFollowupView]

  def countFollowupsForUser(userId: ObjectId): Long

  def countFollowupsForUserSince(date: DateTime, userId: ObjectId): Long

  def findAllFollowupsByCommitForDashboard(): FollowupsByCommitListView
}

