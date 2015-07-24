package com.softwaremill.codebrag.usecases.user

import java.util.UUID

import com.softwaremill.codebrag.domain.{UserToken, Authentication, User}
import com.softwaremill.codebrag.service.user.RegisterService
import com.softwaremill.scalaval.Validation
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId

class RegisterNewUserUseCase(registerService: RegisterService, validator: UserRegistrationValidator) extends Logging {

  def execute(form: RegistrationForm): Either[Validation.Errors, RegisteredUser] = {
    logger.debug(s"Attempting to register new user ${form.login}")
    val firstUser = registerService.isFirstRegistration
    validator.validateRegistration(form, firstUser).whenOk {
      val user = if(firstUser) form.toUser.makeAdmin else form.toUser
      registerService.registerUser(user)
      RegisteredUser(user)
    }
  }

}


case class RegistrationForm(login: String, email: String, password: String, invitationCode: String) {
  lazy val toUser = User(
    new ObjectId,
    Authentication.basic(login, password),
    login,
    email.toLowerCase,
    Set())
}

case class RegisteredUser(id: ObjectId, login: String, email: String)

object RegisteredUser {
  def apply(user: User) = new RegisteredUser(user.id, user.name, user.emailLowerCase)
}
