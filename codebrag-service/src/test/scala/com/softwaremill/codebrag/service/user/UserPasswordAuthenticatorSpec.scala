package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.ObjectIdTestUtils
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.UserToken
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.service.events.MockEventBus
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec}

class UserPasswordAuthenticatorSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter with MockEventBus {

  var userDAO: UserDAO = _
  var authenticator: Authenticator = _
  var userDAOMock: UserDAO = _
  val fixtureLogin = "johndoe"
  val fixturePassword = "password"
  val fixtureUserId: ObjectId = ObjectIdTestUtils.oid(123)
  val fixtureToken = "123abc"
  val ActiveUser = UserAssembler.randomUser
    .withId(fixtureUserId)
    .withBasicAuth(fixtureLogin, fixturePassword)
    .withFullName("John Doe")
    .withEmail("john@doe.com")
    .withToken(fixtureToken)
    .get

  before {
    eventBus.clear()
    userDAOMock = mock[UserDAO]
    authenticator = new UserPasswordAuthenticator(userDAOMock, eventBus)
  }

  it should "call dao to authenticate user" in {
    // given
    given(userDAOMock.findByLoginOrEmail(fixtureLogin)).willReturn(Some(ActiveUser))

    // when
    authenticator.authenticate(fixtureLogin, fixturePassword)

    // then
    verify(userDAOMock) findByLoginOrEmail(fixtureLogin)
  }

  it should "wrap dao response in UserJson" in {
    // given
    given(userDAOMock.findByLoginOrEmail(fixtureLogin)).willReturn(Some(ActiveUser))

    // when
    val Some(result) = authenticator.authenticate(fixtureLogin, fixturePassword)

    // then
    result should equal(ActiveUser)
  }

  it should "return None when user not found" in {
    // given
    given(userDAOMock.findByLoginOrEmail(fixtureLogin)).willReturn(None)

    // when
    val userOpt = authenticator.authenticate(fixtureLogin, fixturePassword)

    // then
    userOpt should be (None)
  }

  it should "return user if user credentials match and user is active" in {
    // given
    given(userDAOMock.findByLoginOrEmail(fixtureLogin)).willReturn(Some(ActiveUser))

    // when
    val Some(user) = authenticator.authenticate(fixtureLogin, fixturePassword)

    user should be(ActiveUser)
  }

  it should "return None if user credentials don't match" in {
    // given
    given(userDAOMock.findByLoginOrEmail(fixtureLogin)).willReturn(Some(ActiveUser))

    // when
    val userOpt = authenticator.authenticate(fixtureLogin, "invalid password")

    userOpt should be(None)
  }

  it should "return None if user found by credentials but is not active" in {
    // given
    val inactiveUser = ActiveUser.copy(active = false)
    given(userDAOMock.findByLoginOrEmail(fixtureLogin)).willReturn(Some(inactiveUser))

    // when
    val userOpt = authenticator.authenticate(fixtureLogin, fixturePassword)

    userOpt should be(None)
  }

  it should "return user if the token matches one of users tokens" in {
    // given
    given(userDAOMock.findByToken(fixtureToken)).willReturn(Some(ActiveUser))

    // when
    val Some(user) = authenticator.authenticateWithToken(fixtureToken)

    // then
    user should be(ActiveUser)
  }

  it should "not return user if token doesn't match" in {
    // given
    given(userDAOMock.findByToken(fixtureToken)).willReturn(None)

    // when
    val userOpt = authenticator.authenticateWithToken(fixtureToken)

    // then
    userOpt should be(None)
  }

  it should "return None if user found by token but is not active" in {
    // given
    val inactiveUser = ActiveUser.copy(active = false)
    given(userDAOMock.findByToken(fixtureToken)).willReturn(Some(inactiveUser))

    // when
    val userOpt = authenticator.authenticateWithToken(fixtureToken)

    // then
    userOpt should be(None)
  }

  it should "remove user's expired tokens while authentication" in {
    // given
    val expiredToken: UserToken = UserToken("token", DateTime.now.minusDays(1))
    val userWithExpiredTokens = ActiveUser.copy(tokens = ActiveUser.tokens + expiredToken)
    given(userDAOMock.findByToken(fixtureToken)).willReturn(Some(userWithExpiredTokens))

    // when
    val Some(user) = authenticator.authenticateWithToken(fixtureToken)

    // then
    user should be(ActiveUser)
    user.tokens should not contain expiredToken
  }

  it should "not authenticate the user based on expired token" in {
    // given
    val userWithExpiredToken = ActiveUser.copy(tokens = Set(UserToken("token", DateTime.now.minusDays(1))))
    given(userDAOMock.findByToken("token")).willReturn(Some(userWithExpiredToken))

    // when
    val userOpt = authenticator.authenticateWithToken("token")

    // then
    userOpt should be(None)
  }
}
