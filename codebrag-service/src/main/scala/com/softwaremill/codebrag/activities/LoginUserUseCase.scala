package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.activities.validation.{ValidateableForm, ValidationErrors, Validation}
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.service.user.AfterUserLogin

case class LoginFailedException(msg: String) extends RuntimeException(msg)

case class LoginForm(login: String, password: String, rememberMe: Boolean)

class LoginUserUseCase(protected val userDao: UserDAO, afterLogin: AfterUserLogin) {

  def execute(loginForm: LoginForm)(doAuthentication: => Option[UserJson]): Either[ValidationErrors, UserJson] = {
    validateUserCanLogin(loginForm).whenNoErrors[UserJson] {
      val authResult = doAuthentication
      authResult.foreach(afterLogin.postLogin)
      authResult.getOrElse(throwAuthFailed("Invalid login credentials"))
    }
  }

  private def validateUserCanLogin(form: LoginForm): Validation = {
    userDao.findByLoginOrEmail(form.login).map { user =>
      val userInactiveCheck = (!user.active, "User account inactive", "general")
      Validation(userInactiveCheck)
    }.getOrElse(throwAuthFailed("Invalid login credentials"))
  }

  private def throwAuthFailed(msg: String) = throw new LoginFailedException(msg)

}
