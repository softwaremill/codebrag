package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.Followup
import org.bson.types.ObjectId

trait FollowupDAO {

  def createOrUpdateExisting(followup: Followup)
  def delete(commitId: ObjectId, userId: ObjectId)

}
