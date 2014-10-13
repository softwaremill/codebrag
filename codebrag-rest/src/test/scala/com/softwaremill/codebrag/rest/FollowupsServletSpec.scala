package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.auth.Scentry
import org.bson.types.ObjectId
import org.mockito.Mockito._
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.usecases.reactions.FollowupDoneUseCase

class FollowupsServletSpec extends AuthenticatableServletSpec {

  val currentUser = UserAssembler.randomUser.get
  var followupFinder = mock[FollowupFinder]
  var useCase = mock[FollowupDoneUseCase]

  override def beforeEach {
    super.beforeEach
    addServlet(new TestableFollowupsServlet(fakeAuthenticator, fakeScentry, followupFinder, useCase), "/*")
  }

  "GET /" should "call backend for list of followups for authenticated user" in {
    userIsAuthenticatedAs(currentUser)
    get("/") {
      status should be (200)
      verify(followupFinder).findAllFollowupsByCommitForUser(currentUser.id)
    }
  }

}

class TestableFollowupsServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[User], followupFinder: FollowupFinder, useCase: FollowupDoneUseCase)
  extends FollowupsServlet(fakeAuthenticator, followupFinder, useCase) {
  override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
}

