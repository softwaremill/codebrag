package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.licence._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.common.Clock

class RegisterLicenceUseCase(licenceService: LicenceService, userDao: UserDAO)(implicit clock: Clock) extends Logging {

  type EnterLicenceKeyResult = Either[String, Licence]
  
  def execute(providedKey: String): EnterLicenceKeyResult = {
    validate(providedKey).right.flatMap { licence =>
      licenceService.updateLicence(licence)
      Right(licence)
    }
  }

  def validate(providedKey: String): EnterLicenceKeyResult = {
    decodeKey(providedKey).right.flatMap { newLicence =>
      val activeUsersCount = userDao.countAllActive().toInt
      if(newLicence.valid(activeUsersCount)) {
        Right(newLicence)
      } else {
        Left("Licence key already expired or too many currently active users")
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
