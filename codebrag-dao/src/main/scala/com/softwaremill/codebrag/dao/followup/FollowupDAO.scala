package com.softwaremill.codebrag.dao.followup

import com.softwaremill.codebrag.domain.Followup
import org.bson.types.ObjectId

trait FollowupDAO {

  def findReceivingUserId(followupId: ObjectId): Option[ObjectId]

  def createOrUpdateExisting(followup: Followup): ObjectId

  def delete(followupId: ObjectId)

}
