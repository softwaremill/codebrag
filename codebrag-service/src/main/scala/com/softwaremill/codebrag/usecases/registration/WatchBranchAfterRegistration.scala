package com.softwaremill.codebrag.usecases.registration

import com.softwaremill.codebrag.usecases.branches.{WatchedBranchForm, StartWatchingBranch, ListRepositoryBranches}
import com.softwaremill.codebrag.service.invitations.InvitationService
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserWatchedBranch

class WatchBranchAfterRegistration(startWatchingBranch: StartWatchingBranch, val invitationService: InvitationService) extends ValidateInvitationCode {

   def execute(invitationCode: String, userId: ObjectId, form: WatchedBranchForm) = {
     val validationResult = validateCode(invitationCode)
     if(validationResult.errors.isEmpty) {
       startWatchingBranch.execute(userId, form)
     } else {
       Left(validationResult.errors)
     }
   }

 }

