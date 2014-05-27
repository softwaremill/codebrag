package com.softwaremill.codebrag.activities

import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.invitations.InvitationService
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.activities.exceptions.PermissionDeniedException
import com.softwaremill.codebrag.activities.assertions.AdminRoleAssertion

class SendInvitationEmailUseCase(invitationService: InvitationService, protected val userDao: UserDAO) extends AdminRoleAssertion {

  def execute(userId: ObjectId, emails: List[String], invitationLink: String) {
    assertUserIsAdmin(userId)
    invitationService.sendInvitation(emails, invitationLink, userId)
  }

}


