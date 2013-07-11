package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.{FollowupsByCommitListView, FollowupView, FollowupListView}

trait FollowupFinder {

  def findAllFollowupsForUser(userId: ObjectId): FollowupListView

  def findAllFollowupsByCommitForUser(userId: ObjectId): FollowupsByCommitListView

  def findFollowupForUser(userId: ObjectId, followupId: ObjectId): Either[String, FollowupView]

}
