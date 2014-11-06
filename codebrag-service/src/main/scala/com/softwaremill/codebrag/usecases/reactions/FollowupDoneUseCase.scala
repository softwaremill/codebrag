package com.softwaremill.codebrag.usecases.reactions

import com.softwaremill.codebrag.service.followups.FollowupService
import org.bson.types.ObjectId

class FollowupDoneUseCase(followupService: FollowupService) {

  type FollowupDoneResult = Either[String, Unit]

  def execute(userId: ObjectId, followupId: ObjectId): FollowupDoneResult = {
    followupService.deleteUserFollowup(userId, followupId)
  }

}
