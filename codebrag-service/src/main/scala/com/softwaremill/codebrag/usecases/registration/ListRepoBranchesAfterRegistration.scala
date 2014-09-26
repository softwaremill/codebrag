package com.softwaremill.codebrag.usecases.registration

import com.softwaremill.codebrag.usecases.branches.{RepositoryBranchesView, ListRepositoryBranches}
import com.softwaremill.codebrag.service.invitations.InvitationService
import org.bson.types.ObjectId

class ListRepoBranchesAfterRegistration(listRepoBranches: ListRepositoryBranches, val invitationService: InvitationService) extends ValidateInvitationCode {

   def execute(invitationCode: String, userId: ObjectId, repoName: String) = {
     validateCode(invitationCode).whenOk[RepositoryBranchesView] {
       listRepoBranches.execute(userId, repoName)
     }
   }

 }

