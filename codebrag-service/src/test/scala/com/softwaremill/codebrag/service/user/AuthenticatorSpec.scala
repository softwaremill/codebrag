package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.{ObjectIdTestUtils, UserDAO}
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.BDDMockito._
import com.softwaremill.codebrag.domain.{Authentication, User}
import com.softwaremill.codebrag.service.data.UserJson
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.events.FakeEventBus
import com.softwaremill.codebrag.service.github.CommitReviewTaskGeneratorActions
import org.eclipse.jgit.util.StringUtils
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import org.mockito.ArgumentCaptor

class AuthenticatorSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter with FakeEventBus {

  var userDAO: UserDAO = _
  var authenticator: Authenticator = _
  var userDAOMock: UserDAO = _
  var reviewTaskGeneratorMock: CommitReviewTaskGeneratorActions = _
  val fixtureLogin = "someLogin"
  val fixtureLoginLowerCase = fixtureLogin.toLowerCase
  val fixturePassword = "somePassword"
  val fixtureUserId: ObjectId = ObjectIdTestUtils.oid(123)
  val fixtureUser = User(fixtureUserId, Authentication.basic(fixtureLogin, fixturePassword), "name", "someLogin@sml.com", "token", "avatarUrl")

  before {
    eventBus.clear()
    userDAOMock = mock[UserDAO]
    reviewTaskGeneratorMock = mock[CommitReviewTaskGeneratorActions]
    authenticator = new Authenticator(userDAOMock, eventBus, reviewTaskGeneratorMock)
  }

  it should "call dao to authenticate user" in {
    // given
    given(userDAOMock.findByLoginOrEmail(fixtureLogin)).willReturn(Some(fixtureUser))
    // when
    authenticator.authenticate(fixtureLogin, fixturePassword)

    // then
    verify(userDAOMock) findByLoginOrEmail(fixtureLogin)
  }

  it should "wrap dao response in UserJson" in {
    // given
    given(userDAOMock.findByLoginOrEmail(fixtureLogin)).willReturn(Some(fixtureUser))

    // when
    val result: Option[UserJson] = authenticator.authenticate(fixtureLogin, fixturePassword)

    // then
    result should equal(Option(UserJson(fixtureUser)))
  }

  it should "return None when user not found" in {
    // given
    given(userDAOMock.findByLowerCasedLogin(fixtureLogin)).willReturn(None)

    // when
    val userOpt = authenticator.authenticate(fixtureLogin, fixturePassword)

    // then
    userOpt should be (None)
  }

}
