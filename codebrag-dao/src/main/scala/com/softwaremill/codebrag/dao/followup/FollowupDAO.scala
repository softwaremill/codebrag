package com.softwaremill.codebrag.dao.followup

import com.softwaremill.codebrag.domain.Followup
import org.bson.types.ObjectId

trait FollowupDAO {

  def findById(followupId: ObjectId): Option[Followup]

  def createOrUpdateExisting(followup: Followup): ObjectId

  def delete(followupId: ObjectId)

}
