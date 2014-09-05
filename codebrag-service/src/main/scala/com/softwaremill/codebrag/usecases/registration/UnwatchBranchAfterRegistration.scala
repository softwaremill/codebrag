package com.softwaremill.codebrag.usecases.registration

import com.softwaremill.codebrag.usecases.branches.{WatchedBranchForm, StopWatchingBranch, StartWatchingBranch}
import com.softwaremill.codebrag.service.invitations.InvitationService
import org.bson.types.ObjectId

class UnwatchBranchAfterRegistration(stopWatchingBranch: StopWatchingBranch, val invitationService: InvitationService) extends ValidateInvitationCode {

   def execute(invitationCode: String, userId: ObjectId, form: WatchedBranchForm) = {
     val validationResult = validateCode(invitationCode)
     if(validationResult.errors.isEmpty) {
       stopWatchingBranch.execute(userId, form)
     } else {
       Left(validationResult.errors)
     }
   }

 }

