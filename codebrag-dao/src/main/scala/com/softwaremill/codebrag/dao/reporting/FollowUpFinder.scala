package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId

trait FollowUpFinder {

  def findAllFollowUpsForUser(userId: ObjectId): FollowUpsList

}
