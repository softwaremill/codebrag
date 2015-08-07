package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.ObjectIdTestUtils
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.PlainUserToken
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
  val fixtureValidToken = PlainUserToken("123abc", DateTime.now.plusDays(1))
  val fixtureExpiredToken = PlainUserToken("token", DateTime.now.minusDays(1))
  val ActiveUser = UserAssembler.randomUser
    .withId(fixtureUserId)
    .withBasicAuth(fixtureLogin, fixturePassword)
    .withFullName("John Doe")
    .withEmail("john@doe.com")
    .withToken(fixtureValidToken)
    .withToken(fixtureExpiredToken)
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
    given(userDAOMock.findByToken(fixtureValidToken.hashed.token)).willReturn(Some(ActiveUser))

    // when
    val Some(user) = authenticator.authenticateWithToken(fixtureValidToken.token)

    // then
    user should be(ActiveUser)
  }

  it should "not return user if token doesn't match" in {
    // given
    given(userDAOMock.findByToken(fixtureValidToken.hashed.token)).willReturn(None)

    // when
    val userOpt = authenticator.authenticateWithToken(fixtureValidToken.token)

    // then
    userOpt should be(None)
  }

  it should "return None if user found by token but is not active" in {
    // given
    val inactiveUser = ActiveUser.copy(active = false)
    given(userDAOMock.findByToken(fixtureValidToken.hashed.token)).willReturn(Some(inactiveUser))

    // when
    val userOpt = authenticator.authenticateWithToken(fixtureValidToken.token)

    // then
    userOpt should be(None)
  }

  it should "not authenticate the user based on expired token" in {
    // given
    given(userDAOMock.findByToken(fixtureExpiredToken.hashed.token)).willReturn(Some(ActiveUser))

    // when
    val userOpt = authenticator.authenticateWithToken(fixtureExpiredToken.token)

    // then
    userOpt should be(None)
  }
}
