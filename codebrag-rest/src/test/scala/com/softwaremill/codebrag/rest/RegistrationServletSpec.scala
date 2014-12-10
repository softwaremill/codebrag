package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.CodebragServletSpec
import com.softwaremill.codebrag.domain.UserWatchedBranch
import com.softwaremill.codebrag.service.user.RegisterService
import com.softwaremill.codebrag.usecases.branches.WatchedBranchForm
import com.softwaremill.codebrag.usecases.registration.{ListRepoBranchesAfterRegistration, ListRepositoriesAfterRegistration, UnwatchBranchAfterRegistration, WatchBranchAfterRegistration}
import com.softwaremill.codebrag.usecases.user.{RegisterNewUserUseCase, RegisteredUser, RegistrationForm}
import com.softwaremill.codebrag.web.CodebragSpecificJSONFormats.SimpleObjectIdSerializer
import org.bson.types.ObjectId
import org.json4s.JsonDSL._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach

class RegistrationServletSpec extends CodebragServletSpec with BeforeAndAfterEach {

  val registerNewUser = mock[RegisterNewUserUseCase]
  val registerService = mock[RegisterService]
  val listRepos = mock[ListRepositoriesAfterRegistration]
  val listRepoBranches = mock[ListRepoBranchesAfterRegistration]
  val watchBranch = mock[WatchBranchAfterRegistration]
  val unwatchBranch = mock[UnwatchBranchAfterRegistration]

  val servlet = new RegistrationServlet(registerService, registerNewUser, listRepos, listRepoBranches, watchBranch, unwatchBranch)

  override def beforeEach {
    reset(registerNewUser, registerService, watchBranch, unwatchBranch)
  }

  "GET /first-registration" should "return firstRegistration flag" in {
    //given
    addServlet(servlet, "/*")
    when(registerService.isFirstRegistration).thenReturn(true)
    //when
    get("/first-registration") {
      //then
      status should be(200)
      body should be(asJson(Map("firstRegistration" -> true)))
    }
  }

  "POST /signup" should "call the register service and return 200 if registration is successful" in {
    addServlet(servlet, "/*")
    val newUser = RegistrationForm("adamw", "adam@example.org", "123456", "code")
    val registered = RegisteredUser(newUser.toUser)
    when(registerNewUser.execute(newUser)).thenReturn(Right(registered))

    post("/signup",
      mapToJson(Map("login" -> "adamw", "email" -> "adam@example.org", "password" -> "123456", "invitationCode" -> "code")),
      defaultJsonHeaders) {
      status should be(200)
    }
  }

  "POST /signup" should "call the register service and return 403 if registration is unsuccessful" in {
    addServlet(servlet, "/*")
    val newUser = RegistrationForm("adamw", "adam@example.org", "123456", "code")
    when(registerNewUser.execute(newUser)).thenReturn(Left(Map.empty[String, Seq[String]]))

    post("/signup",
      mapToJson(Map("login" -> "adamw", "email" -> "adam@example.org", "password" -> "123456", "invitationCode" -> "code")),
      defaultJsonHeaders) {
      status should be(403)
    }
  }

  "POST /signup" should "fallback to empty registration code when one not provided in request" in {
    addServlet(servlet, "/*")
    val newUser = RegistrationForm("adamw", "adam@example.org", "123456", "")
    val registered = RegisteredUser(newUser.toUser)
    when(registerNewUser.execute(newUser)).thenReturn(Right(registered))

    post("/signup",
      mapToJson(Map("login" -> "adamw", "email" -> "adam@example.org", "password" -> "123456")), defaultJsonHeaders) {
      verify(registerNewUser).execute(newUser)
    }
  }

  "POST /repos/repo/branches/branch/watch" should "setup branch as watched" in {
    addServlet(servlet, "/*")
    val invCode = "123abc"
    val repo = "codebrag"
    val branch = "git/flow/style/branch"
    val userId = new ObjectId
    val form = WatchedBranchForm(repo, branch)
    val watched = UserWatchedBranch(new ObjectId, userId, repo, branch)
    when(watchBranch.execute(invCode, userId, form)).thenReturn(Right(watched))

    post(s"/repos/codebrag/branches/git/flow/style/branch/watch?invitationCode=123abc&userId=${userId.toString}") {
      status should be(200)
      body should be(asJson(watched))
    }
  }

  "DELETE /repos/repo/branches/branch/watch" should "setup branch as watched" in {
    addServlet(servlet, "/*")
    val invCode = "123abc"
    val repo = "codebrag"
    val branch = "git/flow/style/branch"
    val userId = new ObjectId
    val form = WatchedBranchForm(repo, branch)
    val watched = UserWatchedBranch(new ObjectId, userId, repo, branch)
    when(unwatchBranch.execute(invCode, userId, form)).thenReturn(Right())

    delete(s"/repos/codebrag/branches/git/flow/style/branch/watch?invitationCode=123abc&userId=${userId.toString}") {
      status should be(200)
    }
  }

  private def asJson[T <: AnyRef](obj: T) = {
    import org.json4s._
    import org.json4s.jackson.Serialization.write
    implicit val formats = DefaultFormats + SimpleObjectIdSerializer
    write(obj)
  }


}
