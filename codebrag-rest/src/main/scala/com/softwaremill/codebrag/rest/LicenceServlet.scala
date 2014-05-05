package com.softwaremill.codebrag.rest

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.licence.LicenceService
import com.softwaremill.codebrag.activities.RegisterLicenceUseCase
import org.scalatra

class LicenceServlet(licenceService: LicenceService, registerUseCase: RegisterLicenceUseCase, val authenticator: Authenticator) extends JsonServletWithAuthentication with Logging {

  get("/") {
    haltIfNotAuthenticated()
    scalatra.Ok(licenceDetails)
  }

  put("/") {
    haltIfNotAuthenticated()
    val licenceKey = (parsedBody \ "licenceKey").extract[String]
    logger.debug(s"Trying to register licence key ${licenceKey}")
    registerUseCase.execute(licenceKey) match {
      case Right(licence) => scalatra.Ok(licenceDetails)
      case Left(msg) => halt(400, Map("error" -> "Invalid licence key provided"))
    }
  }

  private def licenceDetails = {
    Map(
      "valid" -> licenceService.licenceValid,
      "expiresAt" -> licenceService.licenceExpiryDate,
      "daysLeft" -> licenceService.daysToExpire,
      "type" -> licenceService.licenceType,
      "companyName" -> licenceService.companyName
    )
  }

}

object LicenceServlet {
  val MountPath = "licence"
}