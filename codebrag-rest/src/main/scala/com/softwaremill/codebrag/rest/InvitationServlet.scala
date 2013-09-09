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
    val email = (parsedBody \ "email").extract[String]
    val message = (parsedBody \ "invitation").extract[String]
    invitationService.sendInvitation(email, message, new ObjectId(user.id))
  }

  get("/") {
    val invitation = invitationService.createInvitation(new ObjectId(user.id))
    Map("invitation" -> invitation)
  }

}

