package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.invitations.InvitationService
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.user.{RegisterService, Authenticator}

class InvitationServlet(val authenticator: Authenticator, invitationService: InvitationService) extends JsonServletWithAuthentication with Logging {

  before() {
    haltIfNotAuthenticated
  }

  post("/") {
    val emails = (parsedBody \ "emails").extract[List[String]]
    val message = (parsedBody \ "invitationLink").extract[String]
    invitationService.sendInvitation(emails, message, new ObjectId(user.id))
  }

  get("/") {
    Map("invitationCode" -> invitationService.generateInvitationCode(new ObjectId(user.id)))
  }

}

