package com.softwaremill.codebrag.usecases.user

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec}
import com.softwaremill.codebrag.service.user.RegisterService
import com.softwaremill.codebrag.dao.user.UserDAO
import org.mockito.Mockito._
import com.softwaremill.codebrag.service.invitations.InvitationService
import com.softwaremill.scalaval.Validation

class RegisterNewUserUseCaseSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter {

  val validator = mock[UserRegistrationValidator]
  val registerService = mock[RegisterService]
  val useCase: RegisterNewUserUseCase = new RegisterNewUserUseCase(registerService, validator)
  
  val noValidationErrors = Validation.Result(errors = Map.empty)

  before {
    reset(registerService, validator)
  }

  it should "allow first user to be registered" in {
    // given
    val form = RegistrationForm("john", "john@codebrag.com", "secret", "123456")
    when(registerService.isFirstRegistration).thenReturn(true)
    when(validator.validateRegistration(form, firstRegistration = true)).thenReturn(noValidationErrors)

    // when
    useCase.execute(form)

    // then
    verify(registerService).registerUser(form.toUser.makeAdmin)
  }

  it should "allow new user to be registered when validation passes" in {
    // given
    val form = RegistrationForm("john", "john@codebrag.com", "secret", "123456")
    when(registerService.isFirstRegistration).thenReturn(false)
    when(validator.validateRegistration(form, firstRegistration = false)).thenReturn(noValidationErrors)

    // when
    useCase.execute(form)

    // then
    verify(registerService).registerUser(form.toUser)
  }

  it should "not allow new user to be registered when validation fails" in {
    // given
    val form = RegistrationForm("john", "john@codebrag.com", "secret", "123456")
    when(registerService.isFirstRegistration).thenReturn(false)
    val errors = Map("userName" -> Seq("User already exists"))
    when(validator.validateRegistration(form, firstRegistration = false)).thenReturn(Validation.Result(errors))

    // when
    val Left(result) = useCase.execute(form)

    // then
    verify(registerService, times(0)).registerUser(form.toUser)
    result should be(errors)
  }

}
