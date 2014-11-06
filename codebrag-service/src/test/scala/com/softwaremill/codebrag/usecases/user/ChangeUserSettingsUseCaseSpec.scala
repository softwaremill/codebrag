package com.softwaremill.codebrag.usecases.user

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.domain.UserSettings
import org.mockito.Matchers
import com.softwaremill.codebrag.dao.user.UserDAO

class ChangeUserSettingsUseCaseSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {

  var userDao: UserDAO = _
  var changeUserSettings: ChangeUserSettingsUseCase = _

  val dummySettings = IncomingSettings(None, None, None)

  override def beforeEach() {
    userDao = mock[UserDAO]
    changeUserSettings = new ChangeUserSettingsUseCase(userDao)
  }

  it should "update user settings when user found" in {
    // given
    val user = UserAssembler.randomUser.get
    when(userDao.findById(user.id)).thenReturn(Some(user))
    
    // when
    changeUserSettings.execute(user.id, dummySettings)
    
    // then
    verify(userDao).changeUserSettings(Matchers.eq(user.id), Matchers.any[UserSettings])
  }
  
  it should "report error when user not found" in {
    // given
    val nonExistingUser = UserAssembler.randomUser.get
    when(userDao.findById(nonExistingUser.id)).thenReturn(None)

    // when
    val result = changeUserSettings.execute(nonExistingUser.id, dummySettings)

    // then
    result.isLeft should be(true)
  }

}
