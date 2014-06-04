package com.softwaremill.codebrag.activities

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.user.UserDAO
import org.mockito.Matchers._
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.activities.assertions.{ActiveUserStatusRequiredException, AdminRoleRequiredException}

class ModifyUserDetailsUseCaseSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar {

  val userDao = mock[UserDAO]
  val useCase = new ModifyUserDetailsUseCase(userDao)
  
  val TargetUser = UserAssembler.randomUser.get

  after {
    reset(userDao)
  }
  
  it should "not modify user when executing user is neither admin nor active" in {
    // given
    val nonAdminExecutor = UserAssembler.randomUser.withAdmin(set = false).get
    val nonActiveExecutor = UserAssembler.randomUser.withActive(set = false).get
    setupReturningUserFromDB(nonAdminExecutor, nonActiveExecutor)
    val dummyForm = ModifyUserDetailsForm(TargetUser.id, None, None, None)

    // when
    intercept[AdminRoleRequiredException] {
      useCase.execute(nonAdminExecutor.id, dummyForm)
    }
    intercept[ActiveUserStatusRequiredException] {
      useCase.execute(nonActiveExecutor.id, dummyForm)
    }

    // then
    verify(userDao, never()).modifyUser(any[User])
  }

  it should "not allow to change admin and active flags yourself" in {
    // given
    val executor = UserAssembler.randomUser.withAdmin().withActive().get
    setupReturningUserFromDB(executor)

    // when
    val ownChangeForm = ModifyUserDetailsForm(executor.id, None, admin = Some(false), active = Some(false))
    val Left(result) = useCase.execute(executor.id, ownChangeForm)

    // then
    result.fieldErrors should be(Map("userId" -> List("Cannot modify own user")))
    verify(userDao, never()).modifyUser(any[User])
  }

  it should "not allow changing password of inactive user" in {
    // given
    val executor = UserAssembler.randomUser.withAdmin().withActive().get
    val inactiveUser = UserAssembler.randomUser.withActive(set = false).get
    setupReturningUserFromDB(executor, inactiveUser)

    // when
    val form = ModifyUserDetailsForm(inactiveUser.id, newPassword = Some("newSecret"), None, None)
    val Left(result) = useCase.execute(executor.id, form)

    // then
    result.fieldErrors should be(Map("active" -> List("Cannot set password for inactive user")))
    verify(userDao, never()).modifyUser(any[User])
  }

  it should "change details when validation passes" in {
    // given
    val executor = UserAssembler.randomUser.withAdmin().withActive().get
    setupReturningUserFromDB(executor, TargetUser)

    // when
    val form = ModifyUserDetailsForm(TargetUser.id, None, admin = Some(true), active = Some(true))
    val result = useCase.execute(executor.id, form)

    // then
    result should be('right)
    val expectedUser = form.applyTo(TargetUser)
    verify(userDao).modifyUser(expectedUser)
  }

  private def setupReturningUserFromDB(users: User*) {
    users.foreach { user =>
      when(userDao.findById(user.id)).thenReturn(Some(user))
    }
  }

}
