package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.dao.reporting.NotificationCountFinder
import org.mockito.BDDMockito._
import org.mockito.Matchers._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.NotificationCountersView
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.domain.{Authentication, User}

class NotificationCountServletSpec extends AuthenticatableServletSpec {

  val countersFinderMock: NotificationCountFinder = mock[NotificationCountFinder]
  val user = currentUser(new ObjectId)

  override def beforeEach {
    super.beforeEach
    addServlet(new TestableNotificationCountServlet(countersFinderMock), "/*")
  }

  "GET /" should "respond with HTTP 401 when user is not authenticated" in {
    userIsNotAuthenticated
    get("/") {
      status should be(401)
    }
  }

  "GET /" should "call finder to fetch counters for authorized user" in {
    // given
    userIsAuthenticatedAs(UserJson(user))
    val expectedView = new NotificationCountersView(1, 2)
    given(countersFinderMock.getCounters(any[ObjectId])).willReturn(expectedView)
    // when
    get("/") {
      status should be(200)
      body should equal(asJson(expectedView))
    }
  }

  def currentUser(id: ObjectId) = {
    User(id, Authentication.basic("user", "password"), "John Doe", "john@doe.com", "abcde", "avatarUrl")
  }

  class TestableNotificationCountServlet(finder: NotificationCountFinder) extends NotificationCountServlet(fakeAuthenticator, new CodebragSwagger, finder) {

    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry

  }
}


