package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.{RegisterService, Authenticator}
import com.softwaremill.codebrag.service.config.CodebragConfig
import org.bson.types.ObjectId
import org.scalatra
import com.softwaremill.codebrag.usecases.{ModifyUserDetailsUseCase, ModifyUserDetailsForm, UserToRegister, RegisterNewUserUseCase}
import com.softwaremill.codebrag.finders.user.{ManagedUsersListView, UserFinder}

class UsersServlet(
  val authenticator: Authenticator,
  registerService: RegisterService,
  registerUserUseCase: RegisterNewUserUseCase,
  userFinder: UserFinder,
  modifyUserUseCase: ModifyUserDetailsUseCase,
  config: CodebragConfig) extends JsonServletWithAuthentication {

  get("/") {
    haltIfNotAuthenticated()
    if(!config.demo) {
      userFinder.findAllAsManagedUsers()
    } else {
      ManagedUsersListView(List.empty)
    }
  }

  put("/:userId") {
    haltIfNotAuthenticated()
    val targetUserId = new ObjectId(params("userId"))
    val newPassOpt = extractOpt[String]("newPass")
    val adminOpt = extractOpt[Boolean]("admin")
    val activeOpt = extractOpt[Boolean]("active")
    modifyUserUseCase.execute(user.idAsObjectId, ModifyUserDetailsForm(targetUserId, newPassOpt, adminOpt, activeOpt)) match {
      case Left(errors) => scalatra.BadRequest(errors.fieldErrors)
      case _ => scalatra.Ok()
    }
  }

  post("/register") {
    registerUserUseCase.execute(newUser) match {
      case Left(error) => halt(403, error)
      case Right(_) => scalatra.Ok()
    }
  }

  get("/first-registration") {
    Map("firstRegistration" -> registerService.firstRegistration)
  }

  private def newUser = {
    val login = extractNotEmptyString("login")
    val email = extractNotEmptyString("email")
    val password = extractNotEmptyString("password")
    val invitationCode = extractOpt[String]("invitationCode").getOrElse("")
    UserToRegister(login, email, password, invitationCode)
  }

}

object UsersServlet {
  val MappingPath = "users"
}
