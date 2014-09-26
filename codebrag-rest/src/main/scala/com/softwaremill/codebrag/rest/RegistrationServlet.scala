package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.RegisterService
import com.softwaremill.codebrag.usecases.user.{RegistrationForm, RegisterNewUserUseCase}
import org.scalatra
import com.softwaremill.codebrag.usecases.registration.{UnwatchBranchAfterRegistration, WatchBranchAfterRegistration, ListRepoBranchesAfterRegistration, ListRepositoriesAfterRegistration}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.usecases.branches.WatchedBranchForm

class RegistrationServlet(
  registerService: RegisterService,
  registerUserUseCase: RegisterNewUserUseCase,
  listRepos: ListRepositoriesAfterRegistration,
  listRepoBranches: ListRepoBranchesAfterRegistration,
  watchBranch: WatchBranchAfterRegistration,
  unwatchBranch: UnwatchBranchAfterRegistration) extends JsonServlet {

  post("/signup") {
    registerUserUseCase.execute(newUser) match {
      case Left(errors) => scalatra.Forbidden(Map("errors" -> errors))
      case Right(registeredUser) => registeredUser
    }
  }

  get("/repos") {
    val c = extractReqUrlParam("invitationCode")
    listRepos.execute(c) match {
      case Left(errors) => scalatra.Forbidden(Map("errors" -> errors))
      case Right(repos) => repos
    }
  }

  get("/repos/:repo/branches") {
    val c = extractReqUrlParam("invitationCode")
    val u = extractReqUrlParam("userId")
    val r = extractReqUrlParam("repo")
    listRepoBranches.execute(c, new ObjectId(u), r) match {
      case Left(errors) => scalatra.Forbidden(Map("errors" -> errors))
      case Right(branches) => branches
    }
  }

  post("/repos/:repo/branches/:branch/watch") {
    val c = extractReqUrlParam("invitationCode")
    val u = extractReqUrlParam("userId")
    val r = extractReqUrlParam("repo")
    val b = extractReqUrlParam("branch")
    val form = WatchedBranchForm(r, b)
    watchBranch.execute(c, new ObjectId(u), form) match {
      case Left(errors) => scalatra.BadRequest(Map("errors" -> errors))
      case Right(branch) => branch
    }
  }

  delete("/repos/:repo/branches/:branch/watch") {
    val c = extractReqUrlParam("invitationCode")
    val u = extractReqUrlParam("userId")
    val r = extractReqUrlParam("repo")
    val b = extractReqUrlParam("branch")
    val form = WatchedBranchForm(r, b)
    unwatchBranch.execute(c, new ObjectId(u), form) match {
      case Left(errors) => scalatra.BadRequest(Map("errors" -> errors))
      case Right(_) => scalatra.Ok()
    }
  }

  get("/first-registration") {
    Map("firstRegistration" -> registerService.isFirstRegistration)
  }

  private def newUser = {
    val login = extractNotEmptyString("login")
    val email = extractNotEmptyString("email")
    val password = extractNotEmptyString("password")
    val invitationCode = extractOpt[String]("invitationCode").getOrElse("")
    RegistrationForm(login, email, password, invitationCode)
  }

}

object RegistrationServlet {

  val MappingPath = "register"
}