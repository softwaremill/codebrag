package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.licence._

class RegisterLicenceUseCase(licenceService: LicenceService) {

  type EnterLicenceKeyResult = Either[String, Licence]
  
  def execute(providedKey: String): EnterLicenceKeyResult = {
    try {
      val licenceDetails = LicenceEncryptor.decode(providedKey)
      licenceService.updateLicence(licenceDetails)
      Right(licenceDetails)
    } catch {
      case e: InvalidLicenceKeyException => Left("Licence key is incorrect")
    }
  }

}
