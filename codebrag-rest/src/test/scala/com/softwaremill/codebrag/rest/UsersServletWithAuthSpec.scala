package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import org.mockito.Mockito._
import org.bson.types.ObjectId


class UsersServletSpec extends AuthenticatableServletSpec {

  def bindServlet {
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
  }

  "GET /logout" should "call logout() when user is already authenticated" in {
    userIsAuthenticated
    get("/logout") {
      verify(fakeScentry).logout()
    }
  }

  "GET /logout" should "not call logout() when user is not authenticated" in {
    userIsNotAuthenticated
    get("/logout") {
      verify(fakeScentry, never).logout()
      verifyZeroInteractions(fakeAuthenticator)
    }
  }

  "GET /" should "return user information" in {
    val currentUser = UserJson(new ObjectId().toString, "user", "user@email.com", "123abc","avatarUrl")
    userIsAuthenticatedAs(currentUser)
    get("/") {
      status should be (200)
      body should be (asJson(currentUser))
    }
  }

}

class TestableUsersServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
  extends UsersServlet(fakeAuthenticator, new CodebragSwagger) {
  override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
}
