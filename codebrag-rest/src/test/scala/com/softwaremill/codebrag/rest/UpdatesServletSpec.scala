package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.codebrag.service.data.UserJson
import org.mockito.BDDMockito._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{Authentication, User}
import com.softwaremill.codebrag.common.{ClockSpec, Clock}
import com.softwaremill.codebrag.dao.heartbeat.HeartbeatDAO
import com.softwaremill.codebrag.dao.finders.followup.FollowupFinder
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder

class UpdatesServletSpec extends AuthenticatableServletSpec with ClockSpec {

  val toReviewCommitsFinderMock: ToReviewCommitsFinder = mock[ToReviewCommitsFinder]
  val followupFinderMock: FollowupFinder = mock[FollowupFinder]
  val heartbeat: HeartbeatDAO = mock[HeartbeatDAO]
  val user = currentUser(new ObjectId)
  override val fixtureTime = 1000L

  override def beforeEach {
    super.beforeEach
    addServlet(new TestableUpdatesServlet(followupFinderMock, heartbeat, toReviewCommitsFinderMock, clock), "/*")
  }

  "GET /" should "respond with HTTP 401 when user is not authenticated" in {
    userIsNotAuthenticated
    get("/") {
      status should be(401)
    }
  }

  "GET /" should "call finder to fetch counters for authorized user for prepo and branch" in {
    // given
    userIsAuthenticatedAs(UserJson(user))
    val expectedCommits = 1
    val expectedFollowups = 2
    given(followupFinderMock.countFollowupsForUser(user.id)).willReturn(expectedFollowups)
    given(toReviewCommitsFinderMock.count(user.id, Some("codebrag"), Some("master"))).willReturn(expectedCommits)

    // when
    get("/?branch=master&repo=codebrag") {
      //then
      status should equal(200)
      body should include( s""""lastUpdate":$fixtureTime""")
      body should include( s""""commits":$expectedCommits""")
      body should include( s""""followups":$expectedFollowups""")
    }
  }

  def currentUser(id: ObjectId) = User(id, Authentication.basic("user", "password"), "John Doe", "john@doe.com", "abcde")

  class TestableUpdatesServlet(followupFinder: FollowupFinder, heartbeat: HeartbeatDAO, commitFinder: ToReviewCommitsFinder, clock: Clock) extends UpdatesServlet(fakeAuthenticator, followupFinder, heartbeat, commitFinder, clock) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}


