package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.ThreadAwareFollowup
import org.bson.types.ObjectId

trait FollowupDAO {

  def createOrUpdateExisting(followup: ThreadAwareFollowup)

  def delete(commitId: ObjectId, userId: ObjectId)

}
