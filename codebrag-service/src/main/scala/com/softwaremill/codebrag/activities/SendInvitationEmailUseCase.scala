package com.softwaremill.codebrag.activities

import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.invitations.InvitationService
import com.softwaremill.codebrag.dao.user.UserDAO

class SendInvitationEmailUseCase(invitationService: InvitationService, implicit protected val userDao: UserDAO) {

  import com.softwaremill.codebrag.activities.assertions.UserAssertions._

  def execute(userId: ObjectId, emails: List[String], invitationLink: String) {
    assertUserWithId(userId, mustBeAdmin)
    invitationService.sendInvitation(emails, invitationLink, userId)
  }

}


