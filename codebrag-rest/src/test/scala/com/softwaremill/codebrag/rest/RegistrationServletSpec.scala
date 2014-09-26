package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.CodebragServletSpec
import com.softwaremill.codebrag.service.user.RegisterService
import org.mockito.Mockito._
import org.json4s.JsonDSL._
import com.softwaremill.codebrag.usecases.user.{RegisterNewUserUseCase, RegisteredUser, RegistrationForm}
import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.usecases.registration.{UnwatchBranchAfterRegistration, WatchBranchAfterRegistration, ListRepoBranchesAfterRegistration, ListRepositoriesAfterRegistration}

class RegistrationServletSpec extends CodebragServletSpec with BeforeAndAfterEach {

  val registerNewUser = mock[RegisterNewUserUseCase]
  val registerService = mock[RegisterService]
  val listRepos = mock[ListRepositoriesAfterRegistration]
  val listRepoBranches = mock[ListRepoBranchesAfterRegistration]
  val watchBranch = mock[WatchBranchAfterRegistration]
  val unwatchBranch = mock[UnwatchBranchAfterRegistration]

  val servlet = new RegistrationServlet(registerService, registerNewUser, listRepos, listRepoBranches, watchBranch, unwatchBranch)

  override def beforeEach {
    reset(registerNewUser, registerService)
  }

  "GET /first-registration" should "return firstRegistration flag" in {
    //given
    addServlet(servlet, "/*")
    when(registerService.isFirstRegistration).thenReturn(true)
    //when
    get("/first-registration") {
      //then
      status should be(200)
      body should be("{\"firstRegistration\":true}")
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

}
