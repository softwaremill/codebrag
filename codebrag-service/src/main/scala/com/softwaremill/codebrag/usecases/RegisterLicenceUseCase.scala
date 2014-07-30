package com.softwaremill.codebrag.usecases

import com.softwaremill.codebrag.licence._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.scalaval.Validation._


class RegisterLicenceUseCase(licenceService: LicenceService, userDao: UserDAO)(implicit clock: Clock) extends Logging {

  def execute(providedKey: String): Either[Errors, Licence] = {
    decodeKey(providedKey).map { newLicence =>
      validateLicence(newLicence).whenOk[Licence] {
        licenceService.updateLicence(newLicence)
        newLicence
      }
    } getOrElse {
      val incorrectKeyValidation = Map("licenceKey" -> List("Licence key is incorrect"))
      Left(incorrectKeyValidation)
    }
  }

  private def validateLicence(newLicence: Licence) = {
    val activeUsersCount = userDao.countAllActive().toInt
    val usersCountExceeded = rule("general")(newLicence.isValidForUsersCount(activeUsersCount), "Too many currently active users")
    val keyAlreayExpired = rule("general")(newLicence.isValidForCurrentDate, "Licence key already expired")
    validate(usersCountExceeded, keyAlreayExpired)
  }

  private def decodeKey(providedKey: String): Option[Licence] = {
    try {
      Some(LicenceEncryptor.decode(providedKey))
    } catch {
      case e: InvalidLicenceKeyException => {
        logger.error("Could not decode provided key", e.getMessage)
        None
      }
    }
  }
}