package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.{FollowupView, FollowupListView}

trait FollowupFinder {

  def findAllFollowupsForUser(userId: ObjectId): FollowupListView

  def findFollowupForUser(userId: ObjectId, followupId: ObjectId): Either[String, FollowupView]

}
