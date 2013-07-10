package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{NewFollowup, Followup}
import org.bson.types.ObjectId

trait FollowupDAO {

  def findById(followupId: ObjectId): Option[NewFollowup]

  def createOrUpdateExisting(followup: NewFollowup): ObjectId

  def createOrUpdateExisting(followup: Followup): ObjectId

  def delete(followupId: ObjectId)

}
