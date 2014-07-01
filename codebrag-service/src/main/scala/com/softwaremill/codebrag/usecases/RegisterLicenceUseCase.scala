package com.softwaremill.codebrag.usecases

import com.softwaremill.codebrag.licence._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.usecases.validation.{Validation, ValidationErrors}


class RegisterLicenceUseCase(licenceService: LicenceService, userDao: UserDAO)(implicit clock: Clock) extends Logging {

  def execute(providedKey: String): Either[ValidationErrors, Licence] = {
    decodeKey(providedKey).map { newLicence =>
      validate(newLicence).whenNoErrors[Licence] {
        licenceService.updateLicence(newLicence)
        newLicence
      }
    } getOrElse {
      val incorrectKeyValidation = Map("licenceKey" -> List("Licence key is incorrect"))
      Left(ValidationErrors(incorrectKeyValidation))
    }
  }

  private def validate(newLicence: Licence): Validation = {
    val activeUsersCount = userDao.countAllActive().toInt
    val usersCountExceeded = (!newLicence.isValidForUsersCount(activeUsersCount), "Too many currently active users", "general")
    val keyAlreayExpired = (!newLicence.isValidForCurrentDate, "Licence key already expired", "general")
    Validation(usersCountExceeded, keyAlreayExpired)
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