package com.softwaremill.codebrag.usecases

import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.usecases.validation.{ValidationErrors, Validation}
import com.softwaremill.codebrag.finders.user.{LoggedInUserView, UserFinder}
import com.softwaremill.codebrag.domain.User

case class LoginFailedException(msg: String) extends RuntimeException(msg)

case class LoginForm(login: String, password: String, rememberMe: Boolean)

class LoginUserUseCase(protected val userDao: UserDAO, userFinder: UserFinder) {

  def execute(loginForm: LoginForm)(doAuthentication: => Option[User]): Either[ValidationErrors, LoggedInUserView] = {
    validateUserCanLogin(loginForm).whenNoErrors[LoggedInUserView] {
      val authResult = doAuthentication
      authResult.map(userFinder.findLoggedInUser).getOrElse(throwAuthFailed("Invalid login credentials"))
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
