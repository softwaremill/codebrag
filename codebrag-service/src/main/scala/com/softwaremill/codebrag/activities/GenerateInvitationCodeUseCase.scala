package com.softwaremill.codebrag.activities

import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.invitations.InvitationService
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.activities.assertions.AdminRoleRequiredAssertion

class GenerateInvitationCodeUseCase(invitationService: InvitationService, protected val userDao: UserDAO) extends AdminRoleRequiredAssertion {

  def execute(userId: ObjectId): String = {
    assertIsAdmin(userId)
    invitationService.generateInvitationCode(userId)
  }

}
