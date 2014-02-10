package com.softwaremill.codebrag.service.user

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.softwaremill.codebrag.domain.{UserSettings, Authentication, User}
import org.mockito.ArgumentCaptor
import com.softwaremill.codebrag.service.invitations.InvitationService
import com.softwaremill.codebrag.service.notification.NotificationService
import com.softwaremill.codebrag.dao.user.UserDAO

class RegisterServiceTest extends FlatSpec with MockitoSugar with ShouldMatchers {

  val mockUser: User = mock[User]

  it should "register a user" in {
    // Given
    val userDaoMock = mock[UserDAO]
    when(userDaoMock.findByLowerCasedLogin(any())).thenReturn(None)
    when(userDaoMock.findByEmail(any())).thenReturn(None)
    when(userDaoMock.findAll()).thenReturn(List(mockUser))

    val invitationService = mock[InvitationService]
    when(invitationService.verify(any())).thenReturn(true)

    val newUserAdderMock = mock[NewUserAdder]
    val notificationService = mock[NotificationService]

    // When
    val result = new RegisterService(userDaoMock, newUserAdderMock, invitationService, notificationService).register("Adamw", "Adam@example.org", "123456", "code")

    // Then
    result should be('right)

    val userCaptor = ArgumentCaptor.forClass(classOf[User])
    verify(newUserAdderMock).add(userCaptor.capture())
    val user = userCaptor.getValue
    verify(notificationService).sendWelcomeNotification(any[User])

    user.authentication.username should be("Adamw")
    user.authentication.usernameLowerCase should be("adamw")
    user.email should be("adam@example.org")
    user.settings.avatarUrl should equal(UserSettings.defaultAvatarUrl("adam@example.org"))
    user.token.length should be > (0)
    Authentication.passwordsMatch("123456", user.authentication) should be(true)
  }

  it should "register first user" in {
    // Given
    val userDaoMock = mock[UserDAO]
    when(userDaoMock.findByLowerCasedLogin(any())).thenReturn(None)
    when(userDaoMock.findByEmail(any())).thenReturn(None)
    when(userDaoMock.findAll()).thenReturn(Nil)

    val invitationService = mock[InvitationService]

    val newUserAdderMock = mock[NewUserAdder]
    val notificationService = mock[NotificationService]

    // When
    val result = new RegisterService(userDaoMock, newUserAdderMock, invitationService, notificationService).register("Adamw", "Adam@example.org", "123456", "")

    // Then
    result should be('right)

    val userCaptor = ArgumentCaptor.forClass(classOf[User])
    verify(newUserAdderMock).add(userCaptor.capture())
    val user = userCaptor.getValue
    verify(notificationService).sendWelcomeNotification(any[User])

    user.authentication.username should be("Adamw")
    user.authentication.usernameLowerCase should be("adamw")
    user.email should be("adam@example.org")
    user.settings.avatarUrl should equal(UserSettings.defaultAvatarUrl("adam@example.org"))
    user.token.length should be > 0
    Authentication.passwordsMatch("123456", user.authentication) should be(true)
  }

  it should "not register a user if a user with the same login already exists" in {
    // Given
    val userDaoMock = mock[UserDAO]


    when(userDaoMock.findByLowerCasedLogin(any())).thenReturn(Some(mockUser))
    when(userDaoMock.findByEmail(any())).thenReturn(None)
    when(userDaoMock.findAll()).thenReturn(List(mockUser))

    val invitationService = mock[InvitationService]
    when(invitationService.verify(any())).thenReturn(true)

    val notificationService = mock[NotificationService]

    // When
    val result = new RegisterService(userDaoMock, null, invitationService, notificationService).register("adamw", "adam@example.org", "123456", "code")

    // Then
    result should be('left)
  }

  it should "not register a user if a user with the same email already exists" in {
    // Given
    val userDaoMock = mock[UserDAO]

    when(userDaoMock.findByLowerCasedLogin(any())).thenReturn(None)
    when(userDaoMock.findByEmail(any())).thenReturn(Some(mockUser))
    when(userDaoMock.findAll()).thenReturn(List(mockUser))

    val invitationService = mock[InvitationService]
    when(invitationService.verify(any())).thenReturn(true)

    val notificationService = mock[NotificationService]

    // When
    val result = new RegisterService(userDaoMock, null, invitationService, notificationService).register("adamw", "adam@example.org", "123456", "code")

    // Then
    result should be('left)
  }

  it should "not register a user with blank code" in {
    // Given
    val userDaoMock = mock[UserDAO]

    when(userDaoMock.findByLowerCasedLogin(any())).thenReturn(None)
    when(userDaoMock.findByEmail(any())).thenReturn(Some(mockUser))
    when(userDaoMock.findAll()).thenReturn(List(mockUser))

    val invitationService = mock[InvitationService]
    val notificationService = mock[NotificationService]

    // When
    val result = new RegisterService(userDaoMock, null, invitationService, notificationService).register("adamw", "adam@example.org", "123456", "")

    // Then
    result should be('left)
  }

  it should "not register a user with unverifiable code" in {
    // Given
    val userDaoMock = mock[UserDAO]

    when(userDaoMock.findByLowerCasedLogin(any())).thenReturn(None)
    when(userDaoMock.findByEmail(any())).thenReturn(Some(mockUser))
    when(userDaoMock.findAll()).thenReturn(List(mockUser))

    val invitationService = mock[InvitationService]
    when(invitationService.verify("badCode")).thenReturn(false)

    val notificationService = mock[NotificationService]

    // When
    val result = new RegisterService(userDaoMock, null, invitationService, notificationService).register("adamw", "adam@example.org", "123456", "badCode")

    // Then
    result should be('left)
  }
}
