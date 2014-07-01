package com.softwaremill.codebrag.usecases.assertions

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.user.UserDAO
import org.mockito.Mockito
import com.softwaremill.codebrag.domain.builder.UserAssembler

class UserAssertionsSpec extends FlatSpec with MockitoSugar with BeforeAndAfter {

  import UserAssertions._

  var userDao: UserDAO = _

  before {
    userDao = mock[UserDAO]
  }

  it should "throw AdminRoleRequiredException when user is not admin" in {
    // given
    val user = UserAssembler.randomUser.get
    Mockito.when(userDao.findById(user.id)).thenReturn(Some(user))

    // then
    intercept[AdminRoleRequiredException] {
      mustBeAdmin(user)
    }
  }

  it should "throw IllegalStateException when user not found" in {
    // given
    val user = UserAssembler.randomUser.get
    Mockito.when(userDao.findById(user.id)).thenReturn(None)

    // then
    intercept[IllegalStateException] {
      assertUserWithId(user.id, mustBeAdmin)(userDao)
    }
  }

  it should "pass through if user is admin" in {
    // given
    val admin = UserAssembler.randomUser.withAdmin().get
    Mockito.when(userDao.findById(admin.id)).thenReturn(Some(admin))

    // then
    mustBeAdmin(admin)

    // then it passes through
  }

  it should "check multiple assertions" in {
    // given
    val adminNotActive = UserAssembler.randomUser.withAdmin(set = true).withActive(set = false).get
    Mockito.when(userDao.findById(adminNotActive.id)).thenReturn(Some(adminNotActive))

    // then
    intercept[ActiveUserStatusRequiredException] {
      assertUserWithId(adminNotActive.id, mustBeAdmin, mustBeActive)(userDao)
    }
    intercept[ActiveUserStatusRequiredException] {
      assertUser(adminNotActive, mustBeAdmin, mustBeActive)
    }

    // then it passes through

  }
}
