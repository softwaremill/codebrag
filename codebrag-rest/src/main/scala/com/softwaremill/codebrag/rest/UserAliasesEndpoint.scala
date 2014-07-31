package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra
import org.bson.types.ObjectId
import com.softwaremill.codebrag.usecases.emailaliases.{DeleteUserAliasUseCase, AddUserAliasUseCase}

class UserAliasesEndpoint(
  val authenticator: Authenticator,
  val addUserAliasUseCase: AddUserAliasUseCase,
  val deleteUserAliasUseCase: DeleteUserAliasUseCase) extends JsonFilterWithAuthentication {

  get("/:userId/aliases") {
    haltIfNotCurrentUser()
    scalatra.Ok(user.aliases.emailAliases)
  }

  post("/:userId/aliases") {
    haltIfNotCurrentUser()
    val emailAlias = extractReq[String]("email")
    addUserAliasUseCase.execute(user.id, emailAlias) match {
      case Left(errors) => scalatra.BadRequest(errors)
      case Right(aliasCreated) => scalatra.Ok(aliasCreated)
    }
  }

  delete("/:userId/aliases/:aliasId") {
    haltIfNotCurrentUser()
    val aliasIdToRemove = new ObjectId(params("aliasId"))
    deleteUserAliasUseCase.execute(user.id, aliasIdToRemove) match {
      case Left(errors) => scalatra.BadRequest(errors)
      case _ => scalatra.Ok()
    }
  }

  private def haltIfNotCurrentUser() = {
    haltIfNotAuthenticated()
    haltWithForbiddenIf(new ObjectId(params("userId")) != user.id)
  }

}

object UserAliasesEndpoint {
  val MappingPath = "users"
}