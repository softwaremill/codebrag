package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.ThreadAwareFollowup
import org.bson.types.ObjectId

trait FollowupDAO {

  def findById(followupId: ObjectId): Option[ThreadAwareFollowup]

  def createOrUpdateExisting(followup: ThreadAwareFollowup)

  def delete(followupId: ObjectId)

}
