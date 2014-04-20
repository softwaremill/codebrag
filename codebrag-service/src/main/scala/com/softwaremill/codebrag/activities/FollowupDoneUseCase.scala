package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.service.followups.FollowupService
import org.bson.types.ObjectId

class FollowupDoneUseCase(followupService: FollowupService) {

  type FollowupDoneResult = Either[String, Unit]

  def execute(userId: ObjectId, followupId: ObjectId): FollowupDoneResult = {
    ifCanExecute(userId, followupId) {
      followupService.deleteUserFollowup(userId, followupId)
    }
  }

  protected def ifCanExecute(userId: ObjectId, followupId: ObjectId)(block: => FollowupDoneResult): FollowupDoneResult = {
    block
  }

}
