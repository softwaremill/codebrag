package com.softwaremill.codebrag.usecases.user

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.user.UserDAO
import org.mockito.Matchers._
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.{Authentication, User}
import com.softwaremill.codebrag.usecases.assertions.{ActiveUserStatusRequiredException, AdminRoleRequiredException}
import org.bson.types.ObjectId

class DeleteUserUseCaseSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar {

  val userDao = mock[UserDAO]
  val useCase = new DeleteUserUseCase(userDao)
  
  val InactiveUser = UserAssembler.randomUser.withActive(set = false).get
  val ActiveUser = UserAssembler.randomUser.withActive().get

  val ValidExecutor = UserAssembler.randomUser.withAdmin().withActive().get
  val NonAdminExecutor = UserAssembler.randomUser.withActive().withAdmin(set = false).get
  val InactiveExecutor = UserAssembler.randomUser.withActive(set = false).withAdmin().get

  after {
    reset(userDao)
  }
  
  it should "not delete  user when executing user is neither admin nor active" in {
    // given
    setupReturningUserFromDB(NonAdminExecutor, InactiveExecutor)
    val form = DeleteUserForm(ActiveUser.id)

    // when
    intercept[AdminRoleRequiredException] {
      useCase.execute(NonAdminExecutor.id, form)
    }
    intercept[ActiveUserStatusRequiredException] {
      useCase.execute(InactiveExecutor.id, form)
    }

    // then
    verify(userDao, never()).delete(any[ObjectId])
  }

  it should "not allow to delete yourself" in {
    // given
    setupReturningUserFromDB(ValidExecutor)

    // when
    val ownChangeForm = DeleteUserForm(ValidExecutor.id)
    val Left(result) = useCase.execute(ValidExecutor.id, ownChangeForm)

    // then
    result should be(Map("userId" -> List("Cannot delete own user")))
    verify(userDao, never()).delete(any[ObjectId])
  }

  
  it should "delete user when validation passes" in {
    // given
    stubCurrentlyActiveUsersCountTo(0)
    setupReturningUserFromDB(ValidExecutor, ActiveUser)

    // when
    val newAuth = Authentication.basic(ActiveUser.authentication.username, "secret")
    val form = new DeleteUserForm(ActiveUser.id)
    val result = useCase.execute(ValidExecutor.id, form)

    // then
    result should be('right)
    val expectedUser = form
    verify(userDao).delete(expectedUser.userId)
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
  