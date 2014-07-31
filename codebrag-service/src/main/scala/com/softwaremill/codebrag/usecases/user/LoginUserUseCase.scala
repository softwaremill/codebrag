package com.softwaremill.codebrag.usecases.user

import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.finders.user.{LoggedInUserView, UserFinder}
import com.softwaremill.codebrag.domain.User
import com.softwaremill.scalaval.Validation._

case class LoginFailedException(msg: String) extends RuntimeException(msg)

case class LoginForm(login: String, password: String, rememberMe: Boolean)

class LoginUserUseCase(protected val userDao: UserDAO, userFinder: UserFinder) {

  def execute(loginForm: LoginForm)(doAuthentication: => Option[User]): Either[Errors, LoggedInUserView] = {
    validateUserCanLogin(loginForm).whenOk[LoggedInUserView] {
      val authResult = doAuthentication
      authResult.map(userFinder.findLoggedInUser).getOrElse(throwAuthFailed("Invalid login credentials"))
    }
  }

  private def validateUserCanLogin(form: LoginForm) = {
    userDao.findByLoginOrEmail(form.login).map { user =>
      val userInactiveCheck = rule("general")(user.active, "User account inactive")
      validate(userInactiveCheck)
    }.getOrElse(throwAuthFailed("Invalid login credentials"))
  }

  private def throwAuthFailed(msg: String) = throw new LoginFailedException(msg)

}
