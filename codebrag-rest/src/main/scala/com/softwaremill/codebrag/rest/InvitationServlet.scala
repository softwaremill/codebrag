package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.usecases.{SendInvitationEmailUseCase, GenerateInvitationCodeUseCase}

class InvitationServlet(
  val authenticator: Authenticator,
  generateInvitationCodeUseCase: GenerateInvitationCodeUseCase,
  sendInvitationEmailUseCase: SendInvitationEmailUseCase) extends JsonServletWithAuthentication with Logging {

  before() {
    haltIfNotAuthenticated
  }

  post("/") {
    val emails = (parsedBody \ "emails").extract[List[String]]
    val invitationLink = (parsedBody \ "invitationLink").extract[String]
    sendInvitationEmailUseCase.execute(user.id, emails, invitationLink)
  }

  get("/") {
    val invitationCode = generateInvitationCodeUseCase.execute(user.id)
    Map("invitationCode" -> invitationCode)
  }

}

