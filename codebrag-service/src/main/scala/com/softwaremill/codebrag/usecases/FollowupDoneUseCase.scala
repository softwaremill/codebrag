package com.softwaremill.codebrag.usecases

import com.softwaremill.codebrag.service.followups.FollowupService
import org.bson.types.ObjectId
import com.softwaremill.codebrag.licence.LicenceService

class FollowupDoneUseCase(followupService: FollowupService, licenceService: LicenceService) {

  type FollowupDoneResult = Either[String, Unit]

  def execute(userId: ObjectId, followupId: ObjectId): FollowupDoneResult = {
    ifCanExecute(userId, followupId) {
      followupService.deleteUserFollowup(userId, followupId)
    }
  }

  protected def ifCanExecute(userId: ObjectId, followupId: ObjectId)(block: => FollowupDoneResult): FollowupDoneResult = {
    licenceService.interruptIfLicenceExpired()
    block
  }

}
