package com.softwaremill.codebrag.dao.finders.followup

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.{FollowupsByCommitListView, SingleFollowupView}

trait FollowupFinder {

  def findAllFollowupsByCommitForUser(userId: ObjectId): FollowupsByCommitListView

  def findFollowupForUser(userId: ObjectId, followupId: ObjectId): Either[String, SingleFollowupView]

}
