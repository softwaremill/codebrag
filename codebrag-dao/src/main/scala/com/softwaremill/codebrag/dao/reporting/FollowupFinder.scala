package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId

trait FollowupFinder {

  def findAllFollowupsForUser(userId: ObjectId): FollowupsList

}
