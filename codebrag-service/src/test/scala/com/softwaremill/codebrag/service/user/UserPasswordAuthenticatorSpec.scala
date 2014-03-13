package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.ObjectIdTestUtils
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.BDDMockito._
import com.softwaremill.codebrag.domain.{Authentication, User}
import com.softwaremill.codebrag.service.data.UserJson
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.events.MockEventBus
import com.softwaremill.codebrag.service.commits.AfterUserRegisteredHook
import com.softwaremill.codebrag.dao.user.UserDAO

class UserPasswordAuthenticatorSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter with MockEventBus {

  var userDAO: UserDAO = _
  var authenticator: Authenticator = _
  var userDAOMock: UserDAO = _
  val fixtureLogin = "johndoe"
  val fixturePassword = "password"
  val fixtureUserId: ObjectId = ObjectIdTestUtils.oid(123)
  val fixtureUser = User(fixtureUserId, Authentication.basic(fixtureLogin, fixturePassword), "John Doe", "john@doe.com", "123abc", "http://gravatar.com")

  before {
    eventBus.clear()
    userDAOMock = mock[UserDAO]
    authenticator = new UserPasswordAuthenticator(userDAOMock, eventBus)
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
    given(userDAOMock.findByLoginOrEmail(fixtureLogin)).willReturn(None)

    // when
    val userOpt = authenticator.authenticate(fixtureLogin, fixturePassword)

    // then
    userOpt should be (None)
  }

  it should "return user if user credentials match" in {
    // given
    given(userDAOMock.findByLoginOrEmail(fixtureLogin)).willReturn(Some(fixtureUser))

    // when
    val Some(user) = authenticator.authenticate(fixtureLogin, fixturePassword)

    user.fullName should be(fixtureUser.name)
    user.email should be(fixtureUser.emailLowerCase)
  }

  it should "return None if user credentials don't match" in {
    // given
    given(userDAOMock.findByLoginOrEmail(fixtureLogin)).willReturn(Some(fixtureUser))

    // when
    val userOpt = authenticator.authenticate(fixtureLogin, "invalid password")

    userOpt should be(None)
  }
}
