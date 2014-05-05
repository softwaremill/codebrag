package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.licence._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO

class RegisterLicenceUseCase(licenceService: LicenceService, userDao: UserDAO) extends Logging {

  type EnterLicenceKeyResult = Either[String, Licence]
  
  def execute(providedKey: String): EnterLicenceKeyResult = {
    validate(providedKey).right.flatMap { licence =>
      licenceService.updateLicence(licence)
      Right(licence)
    }
  }

  def validate(providedKey: String): EnterLicenceKeyResult = {
    decodeKey(providedKey).right.flatMap { licence =>
      if(userDao.countAll() > licence.maxUsers) {
        Left("Invalid licence - too many users registered")
      } else {
        Right(licence)
      }
    }
  }

  private def decodeKey(providedKey: String): EnterLicenceKeyResult = {
    try {
      Right(LicenceEncryptor.decode(providedKey))
    } catch {
      case e: InvalidLicenceKeyException => {
        logger.error("Could not register provided key", e.getMessage)
        Left("Licence key is incorrect")
      }
    }
  }

}
