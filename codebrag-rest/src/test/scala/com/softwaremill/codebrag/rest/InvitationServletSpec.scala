package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import org.scalatest.BeforeAndAfterEach
import org.mockito.Mockito._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.user.UserJsonBuilder._
import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import org.mockito.ArgumentCaptor
import com.softwaremill.codebrag.activities.{SendInvitationEmailUseCase, GenerateInvitationCodeUseCase}
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.activities.exceptions.PermissionDeniedException


class InvitationServletSpec extends AuthenticatableServletSpec with BeforeAndAfterEach {

  var generateCodeUseCase: GenerateInvitationCodeUseCase = _
  var sendInvitationUseCase: SendInvitationEmailUseCase = _

  override def beforeEach {
    super.beforeEach()
    generateCodeUseCase = mock[GenerateInvitationCodeUseCase]
    sendInvitationUseCase = mock[SendInvitationEmailUseCase]
    addServlet(new TestableInvitationServlet(fakeAuthenticator, fakeScentry, generateCodeUseCase, sendInvitationUseCase), "/*")
  }

  "GET /" should "return invitation message if user is admin" in {
    //given
    val adminUser = UserJson(UserAssembler.randomUser.withAdmin().get)
    userIsAuthenticatedAs(adminUser)
    val invitationCode = "123abc"
    when(generateCodeUseCase.execute(adminUser.idAsObjectId)).thenReturn(invitationCode)
    //when
    get("/") {
      //then
      status should be(200)
      body should be("{\"invitationCode\":\""+invitationCode+"\"}")
    }
  }

  "GET /" should "return permission denied if user is not allowed to invite others" in {
    userIsAuthenticatedAs(someUser)
    when(generateCodeUseCase.execute(someUser.idAsObjectId)).thenThrow(new PermissionDeniedException("Action not allowed"))
    //when
    get("/") {
      //then
      status should be(403)
    }
  }

  "POST /" should "send invitation if user is admin" in {
    //given
    val adminUser = UserJson(UserAssembler.randomUser.withAdmin().get)
    userIsAuthenticatedAs(adminUser)
    val email = "adam@example.org"
    val invitationLink = "http://codebrag.com/#/register/123abc123"

    //when
    val json = s"""{"invitationLink": "${invitationLink}", "emails": ["${email}"]}"""
    post("/", json, defaultJsonHeaders) {
      //then
      status should be(200)
      val emailsCaptor = ArgumentCaptor.forClass(classOf[List[String]])
      val linkCaptor = ArgumentCaptor.forClass(classOf[String])
      val userIdCaptor = ArgumentCaptor.forClass(classOf[ObjectId])
      verify(sendInvitationUseCase).execute(userIdCaptor.capture(), emailsCaptor.capture(),linkCaptor.capture())
      emailsCaptor.getValue should be(List(email))
      linkCaptor.getValue should be (invitationLink)
    }
  }

  "POST /" should "return permission denied if user has no permission to send invitation" in {
    //given
    userIsAuthenticatedAs(someUser)
    val email = "adam@example.org"
    val invitationLink = "http://codebrag.com/#/register/123abc123"
    when(sendInvitationUseCase.execute(someUser.idAsObjectId, List(email), invitationLink)).thenThrow(new PermissionDeniedException("Action not allowed"))

    //when
    val json = s"""{"invitationLink": "${invitationLink}", "emails": ["${email}"]}"""
    post("/", json, defaultJsonHeaders) {
      //then
      status should be(403)
    }
  }

}

class TestableInvitationServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson], generateCode: GenerateInvitationCodeUseCase, sendEmail: SendInvitationEmailUseCase)
  extends InvitationServlet(fakeAuthenticator, generateCode, sendEmail) {
  override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
}

