package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.{RegisterService, Authenticator}
import com.softwaremill.codebrag.service.config.CodebragConfig
import org.bson.types.ObjectId
import org.scalatra
import com.softwaremill.codebrag.finders.user.UserFinder
import com.softwaremill.codebrag.finders.user.ManagedUsersListView
import com.softwaremill.codebrag.usecases.user.{RegisterNewUserUseCase, ModifyUserDetailsUseCase, ModifyUserDetailsForm,DeleteUserUseCase,DeleteUserForm}

class UsersServlet(
  val authenticator: Authenticator,
  userFinder: UserFinder,
  modifyUserUseCase: ModifyUserDetailsUseCase,
  deleteUserUseCase: DeleteUserUseCase,
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
    modifyUserUseCase.execute(user.id, ModifyUserDetailsForm(targetUserId, newPassOpt, adminOpt, activeOpt)) match {
      case Left(errors) => scalatra.BadRequest(errors)
      case _ => scalatra.Ok()
    }
  }
delete("/:userId") {
  haltIfNotAuthenticated()
    val targetUserId = new ObjectId(params("userId"))   
    deleteUserUseCase.execute(user.id, DeleteUserForm(targetUserId)) match {
      case Left(errors) => scalatra.BadRequest(errors)
      case _ => scalatra.Ok()
    }
  }
}
  

object UsersServlet {
  val MappingPath = "users"
}
