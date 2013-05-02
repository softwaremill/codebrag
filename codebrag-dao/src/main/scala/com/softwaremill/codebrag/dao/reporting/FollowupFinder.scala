package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.FollowupListView

trait FollowupFinder {

  def findAllFollowupsForUser(userId: ObjectId): FollowupListView

}
