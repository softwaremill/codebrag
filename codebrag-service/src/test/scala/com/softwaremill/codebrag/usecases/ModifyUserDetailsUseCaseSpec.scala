package com.softwaremill.codebrag.usecases

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.user.UserDAO
import org.mockito.Matchers._
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.{Authentication, User}
import com.softwaremill.codebrag.usecases.assertions.{ActiveUserStatusRequiredException, AdminRoleRequiredException}
import com.softwaremill.codebrag.licence.LicenceService

class ModifyUserDetailsUseCaseSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar {

  val userDao = mock[UserDAO]
  val licenceService = mock[LicenceService]
  val useCase = new ModifyUserDetailsUseCase(userDao, licenceService)
  
  val InactiveUser = UserAssembler.randomUser.withActive(set = false).get
  val ActiveUser = UserAssembler.randomUser.withActive().get

  val ValidExecutor = UserAssembler.randomUser.withAdmin().withActive().get
  val NonAdminExecutor = UserAssembler.randomUser.withActive().withAdmin(set = false).get
  val InactiveExecutor = UserAssembler.randomUser.withActive(set = false).withAdmin().get

  after {
    reset(userDao, licenceService)
  }
  
  it should "not modify user when executing user is neither admin nor active" in {
    // given
    setupReturningUserFromDB(NonAdminExecutor, InactiveExecutor)
    val form = ModifyUserDetailsForm(ActiveUser.id, None, None, None)

    // when
    intercept[AdminRoleRequiredException] {
      useCase.execute(NonAdminExecutor.id, form)
    }
    intercept[ActiveUserStatusRequiredException] {
      useCase.execute(InactiveExecutor.id, form)
    }

    // then
    verify(userDao, never()).modifyUser(any[User])
  }

  it should "not allow to change admin and active flags yourself" in {
    // given
    setupReturningUserFromDB(ValidExecutor)

    // when
    val ownChangeForm = ModifyUserDetailsForm(ValidExecutor.id, None, admin = Some(false), active = Some(false))
    val Left(result) = useCase.execute(ValidExecutor.id, ownChangeForm)

    // then
    result should be(Map("userId" -> List("Cannot modify own user")))
    verify(userDao, never()).modifyUser(any[User])
  }

  it should "not allow changing password of inactive user" in {
    // given
    setupReturningUserFromDB(ValidExecutor, InactiveUser)

    // when
    val form = ModifyUserDetailsForm(InactiveUser.id, newPassword = Some("newSecret"), None, None)
    val Left(result) = useCase.execute(ValidExecutor.id, form)

    // then
    result should be(Map("active" -> List("Cannot set password for inactive user")))
    verify(userDao, never()).modifyUser(any[User])
  }

  it should "not allow making user active if it would exceed licenced active users count" in {
    // given
    stubLicenceMaxUsersTo(1)
    stubCurrentlyActiveUsersCountTo(1)
    setupReturningUserFromDB(ValidExecutor, InactiveUser)

    // when
    val formWithActiveFlag = ModifyUserDetailsForm(InactiveUser.id, newPassword = None, admin = None, active = Some(true))
    val Left(result) = useCase.execute(ValidExecutor.id, formWithActiveFlag)

    // then
    result should be(Map("active" -> List("Licenced active users count exceeded")))
    verify(userDao, never()).modifyUser(any[User])
  }

  it should "change details when validation passes" in {
    // given
    stubLicenceMaxUsersTo(1)
    stubCurrentlyActiveUsersCountTo(0)
    setupReturningUserFromDB(ValidExecutor, ActiveUser)

    // when
    val newAuth = Authentication.basic(ActiveUser.authentication.username, "secret")
    val form = new ModifyUserDetailsForm(ActiveUser.id, newPassword = Some("secret"), admin = Some(true), active = None) {
      override def buildNewAuth(username: String, password: String) = newAuth
    }
    val result = useCase.execute(ValidExecutor.id, form)

    // then
    result should be('right)
    val expectedUser = form.applyTo(ActiveUser)
    verify(userDao).modifyUser(expectedUser)
  }

  private def stubLicenceMaxUsersTo(maxUsers: Int) {
    when(licenceService.maxUsers).thenReturn(maxUsers)
  }

  private def stubCurrentlyActiveUsersCountTo(activeUsersCount: Int) {
    when(userDao.countAllActive()).thenReturn(activeUsersCount)
  }

  private def setupReturningUserFromDB(users: User*) {
    users.foreach { user =>
      when(userDao.findById(user.id)).thenReturn(Some(user))
    }
  }

}
