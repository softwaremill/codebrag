package com.softwaremill.codebrag.rest

import org.scalatra._
import com.softwaremill.codebrag.service.user.{AfterUserLogin, RegisterService, Authenticator}
import com.softwaremill.codebrag.service.data.UserJson
import swagger.{Swagger, SwaggerSupport}
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.dao.user.UserDAO
import org.bson.types.ObjectId
import com.softwaremill.codebrag.cache.UserReviewedCommitsCache
import org.scalatra
import com.softwaremill.codebrag.activities.{UserToRegister, RegisterNewUserUseCase}
import com.softwaremill.codebrag.dao.finders.user.{ManagedUsersListView, UserFinder}

class UsersServlet(
  val authenticator: Authenticator,
  registerService: RegisterService,
  registerUserUseCase: RegisterNewUserUseCase,
  userFinder: UserFinder,
  config: CodebragConfig) extends JsonServletWithAuthentication {

  get("/") {
    haltIfNotAuthenticated()
    if(!config.demo) {
      userFinder.findAllAsManagedUsers()
    } else {
      ManagedUsersListView(List.empty)
    }
  }

  post("/register") {
    registerUserUseCase.execute(newUser) match {
      case Left(error) => halt(403, error)
      case Right(_) => scalatra.Ok
    }
  }

  get("/first-registration") {
    Map("firstRegistration" -> registerService.firstRegistration)
  }

  private def newUser = {
    val login = extractNotEmptyString("login")
    val email = extractNotEmptyString("email")
    val password = extractNotEmptyString("password")
    val invitationCode = extractNotEmptyString("invitationCode")
    UserToRegister(login, email, password, invitationCode)
  }

}

object UsersServlet {
  val MappingPath = "users"
}
