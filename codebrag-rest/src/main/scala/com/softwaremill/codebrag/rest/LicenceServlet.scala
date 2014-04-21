package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.licence.LicenceService

class LicenceServlet(licenceService: LicenceService, val authenticator: Authenticator) extends JsonServletWithAuthentication with Logging {

  get("/") {
    haltIfNotAuthenticated()
    Map("valid" -> licenceService.licenceValid, "expiresAt" -> licenceService.licenceExpiryDate)
  }

}

object LicenceServlet {
  val MountPath = "licence"
}