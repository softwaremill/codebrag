package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.{User, UserSettings}
import com.softwaremill.codebrag.finders.browsingcontext.{UserBrowsingContext, UserBrowsingContextFinder}
import com.softwaremill.codebrag.service.user.Authenticator
import com.softwaremill.codebrag.usecases.{UpdateUserBrowsingContextForm, UpdateUserBrowsingContextUseCase}
import com.softwaremill.codebrag.usecases.user.{ChangeUserSettingsUseCase, IncomingSettings}
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatra.auth.Scentry


class BrowsingContextServletSpec extends AuthenticatableServletSpec with BeforeAndAfterEach {

  val authenticator: Authenticator = mock[Authenticator]
  val contextFinder: UserBrowsingContextFinder = mock[UserBrowsingContextFinder]
  var updateContext: UpdateUserBrowsingContextUseCase = mock[UpdateUserBrowsingContextUseCase]

  var currentUser: User = _

  override def beforeEach {
    super.beforeEach
    Mockito.reset(contextFinder, updateContext)
    addServlet(new TestableBrowsingContextServlet(fakeAuthenticator, fakeScentry), "/*")
    currentUser = UserAssembler.randomUser.get
    userIsAuthenticatedAs(currentUser)
  }

  "GET /" should "find set of all browsing contexts for user" in {
    // given
    val ubc = Set(UserBrowsingContext(currentUser.id, "codebrag", "master"))
    when(contextFinder.findAll(currentUser.id)).thenReturn(ubc)

    // when
    get("/") {
      // then
      body should be(asJson(ubc))
      status should be(200)
    }
  }

  "GET /:repo" should "find user's browsing contexts for given repo" in {
    // given
    val ubc = UserBrowsingContext(currentUser.id, "codebrag", "master")
    when(contextFinder.find(currentUser.id, "codebrag")).thenReturn(Some(ubc))
    when(contextFinder.find(currentUser.id, "bootzooka")).thenReturn(None)

    // when
    get("/codebrag") {
      // then
      body should be(asJson(ubc))
      status should be(200)
    }

    // when
    get("/bootzooka") {
      // then
      status should be(404)
    }
  }

  "PUT /:repo" should "update user's browsing contexts for given repo" in {
    // given
    val codebragForm = UpdateUserBrowsingContextForm(currentUser.id, "codebrag", "master")
    when(updateContext.execute(codebragForm)).thenReturn(Right())
    val bootzookaForm = UpdateUserBrowsingContextForm(currentUser.id, "bootzooka", "master")
    val errors = Map("repo" -> List("Unknown repo"))
    when(updateContext.execute(bootzookaForm)).thenReturn(Left(errors))

    // when
    put("/codebrag", """{"branch": "master"}""", defaultJsonHeaders) {
      // then
      body should be('empty)
      status should be(200)
    }

    // when
    put("/bootzooka", """{"branch": "master"}""", defaultJsonHeaders) {
      // then
      body should be(asJson(Map("errors" -> errors)))
      status should be(400)
    }
  }

  class TestableBrowsingContextServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[User])
    extends BrowsingContextServlet(fakeAuthenticator, contextFinder, updateContext) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
