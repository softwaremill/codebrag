package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.licence._
import com.typesafe.scalalogging.slf4j.Logging

class RegisterLicenceUseCase(licenceService: LicenceService) extends Logging {

  type EnterLicenceKeyResult = Either[String, Licence]
  
  def execute(providedKey: String): EnterLicenceKeyResult = {
    try {
      val licenceDetails = LicenceEncryptor.decode(providedKey)
      licenceService.updateLicence(licenceDetails)
      Right(licenceDetails)
    } catch {
      case e: InvalidLicenceKeyException => {
        logger.error("Could not register provided key", e.getMessage)
        Left("Licence key is incorrect")
      }
    }
  }

}
