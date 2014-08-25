package com.softwaremill.codebrag.usecases.user

import com.softwaremill.codebrag.licence._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.service.user.RegisterService
import com.softwaremill.scalaval.Validation

class RegisterNewUserUseCase(licenceService: LicenceService, registerService: RegisterService, userDao: UserDAO) extends Logging {

  def execute(user: UserToRegister): Either[Validation.Errors, Unit] = {
    logger.debug(s"Attempting to register new user ${user.login}")
    validateRegistration().whenOk {
      registerService.register(user.login, user.email, user.password, user.invitationCode)
    }
  }

  def validateRegistration() = {
    import com.softwaremill.scalaval.Validation._
    val licence = rule("licence")(licenceService.maxUsers > userDao.countAllActive(), RegisterNewUserUseCase.MaxUsersExceededMessage)
    validate(licence)
  }

}

object RegisterNewUserUseCase {
  val MaxUsersExceededMessage = "Unable to register new user - maximum number of licensed users exceeded"
}

case class UserToRegister(login: String, email: String, password: String, invitationCode: String)