package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.UserDAO
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.BDDMockito._
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.service.data.UserJson

class UserServiceSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfter {

  var userDAO: UserDAO = _
  var userService: UserService = _
  var userDAOMock: UserDAO = _
  val fixtureUser = User("someLogin", "someLogin@sml.com", "somePassword", "salt", "token")
  val fixtureLogin: String = "someLogin"
  val fixturePassword: String = "somePassword"

  before {
    userDAOMock = mock[UserDAO]
    userService = new UserService(userDAOMock)
  }

  it should "call dao to authenticate user" in {
    // when
    userService.authenticate(fixtureLogin, fixturePassword)

    // then
    verify(userDAOMock) findByLoginOrEmail(fixtureLogin)
  }

  it should "wrap dao response in UserJson" in {
    // given
    given(userDAOMock.findByLoginOrEmail(fixtureLogin)).willReturn(Some(fixtureUser))

    // when
    val result: Option[UserJson] = userService.authenticate(fixtureLogin, fixturePassword)

    // then
    result should equal(Option(UserJson(fixtureUser)))
  }

}
