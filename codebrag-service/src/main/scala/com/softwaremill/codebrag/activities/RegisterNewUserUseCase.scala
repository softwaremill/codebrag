package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.licence._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.service.user.RegisterService

class RegisterNewUserUseCase(licenceService: LicenceService, registerService: RegisterService, userDao: UserDAO) extends Logging {

  type UserRegistrationResult = Either[String, Unit]
  
  def execute(user: UserToRegister): UserRegistrationResult = {
    logger.debug(s"Attempting to register new user ${user.login}")
    validate.right.flatMap { _ =>
      registerService.register(user.login, user.email, user.password, user.invitationCode)
    }
  }

  def validate: UserRegistrationResult = {
    if(licenceService.maxUsers > userDao.countAll()) {
      Right()
    } else {
      logger.debug(s"Cannot register user - max users licenced reached")
      Left(RegisterNewUserUseCase.MaxUsersExceededMessage)
    }
  }

}

object RegisterNewUserUseCase {
  val MaxUsersExceededMessage = "Unable to register new user - maximum number of licensed users exceeded"
}

case class UserToRegister(login: String, email: String, password: String, invitationCode: String)