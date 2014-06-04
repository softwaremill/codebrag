package com.softwaremill.codebrag.activities.assertions

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.user.UserDAO
import org.mockito.Mockito
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.activities.assertions.AdminRoleRequiredException

class AdminRoleAssertionSpec extends FlatSpec with MockitoSugar with BeforeAndAfter {

  var _userDao: UserDAO = _
  var assertion: AdminRoleRequiredAssertion = _

  before {
    _userDao = mock[UserDAO]
    assertion = new AdminRoleRequiredAssertion {
      val userDao = _userDao
    }
  }

  it should "throw permission denied exception when user is not admin" in {
    // given
    val user = UserAssembler.randomUser.get
    Mockito.when(_userDao.findById(user.id)).thenReturn(Some(user))

    // then
    intercept[AdminRoleRequiredException] {
      assertion.assertIsAdmin(user.id)
    }
  }

  it should "throw illegal state exception when user not found" in {
    // given
    val user = UserAssembler.randomUser.get
    Mockito.when(_userDao.findById(user.id)).thenReturn(None)

    // then
    intercept[IllegalStateException] {
      assertion.assertIsAdmin(user.id)
    }
  }

  it should "pass through if user is admin" in {
    // given
    val admin = UserAssembler.randomUser.withAdmin().get
    Mockito.when(_userDao.findById(admin.id)).thenReturn(Some(admin))

    // when
    assertion.assertIsAdmin(admin.id)

    // then it passes through
  }
}
