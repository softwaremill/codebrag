package com.softwaremill.codebrag.activities

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.service.user.AfterUserLogin
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.service.data.UserJson

class LoginUserUseCaseSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar {

  val userDao = mock[UserDAO]
  val afterLoginHook = mock[AfterUserLogin]
  val loginUseCase = new LoginUserUseCase(userDao, afterLoginHook)

  before {
    reset(userDao, afterLoginHook)
  }

  it should "not try to authenticate when user is inactive" in {
    // given
    val inactiveUser = UserAssembler.randomUser.withActive(set = false).get
    when(userDao.findByLoginOrEmail(inactiveUser.emailLowerCase)).thenReturn(Some(inactiveUser))

    // when
    val loginForm = LoginForm(inactiveUser.emailLowerCase, "dummy", false)
    val Left(result) = loginUseCase.execute(loginForm) {
      fail("Authenticatin block should not be called when user is inactive")
    }

    // then
    val expectedErrors = Map("general" -> List("User account inactive"))
    result.fieldErrors should be(expectedErrors)
  }

  it should "not try to authenticate when user not found by login/email" in {
    // given
    val nonExistingUser = UserAssembler.randomUser.withActive(set = false).get
    when(userDao.findByLoginOrEmail(nonExistingUser.emailLowerCase)).thenReturn(None)

    // when
    val loginForm = LoginForm(nonExistingUser.emailLowerCase, "dummy", false)
    val exceptionCaught = intercept[LoginFailedException] {
      loginUseCase.execute(loginForm) {
        fail("Authenticatin block should not be called when user is inactive")
      }
    }

    // then
    exceptionCaught.msg should be("Invalid login credentials")
  }

  it should "invoke post login hook when user authenticates successfuly" in {
    // given
    val user = UserAssembler.randomUser.get
    val authenticatedUserJson = UserJson(user)
    when(userDao.findByLoginOrEmail(user.emailLowerCase)).thenReturn(Some(user))

    // when
    val loginForm = LoginForm(user.emailLowerCase, "dummy", false)
    loginUseCase.execute(loginForm) { Some(authenticatedUserJson) }

    // then
    verify(afterLoginHook).postLogin(authenticatedUserJson)
  }

  it should "raise exception when use cannot authenticate due to bad credentials" in {
    // given
    val user = UserAssembler.randomUser.get
    when(userDao.findByLoginOrEmail(user.emailLowerCase)).thenReturn(Some(user))

    // when
    val loginForm = LoginForm(user.emailLowerCase, "dummy", false)
    val exceptionCaught = intercept[LoginFailedException] {
      loginUseCase.execute(loginForm) { None }
    }

    // then
    exceptionCaught.msg should be("Invalid login credentials")
  }

}
