package com.softwaremill.codebrag.usecases.registration

import com.softwaremill.codebrag.service.invitations.InvitationService

private[registration] trait ValidateInvitationCode {

  def invitationService: InvitationService

  def validateCode(c: String) = {
    import com.softwaremill.scalaval.Validation._
    val codeValid = rule("invitationCode")(invitationService.verify(c), "Invalid invitation code")
    validate(codeValid)
  }

}
