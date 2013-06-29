package com.softwaremill.codebrag.service.user

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.UserDAO
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.softwaremill.codebrag.domain.{Authentication, User}
import org.mockito.ArgumentCaptor

class RegisterServiceTest extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach {
  it should "register a user" in {
    // Given
    val userDaoMock = mock[UserDAO]
    when(userDaoMock.findByLowerCasedLogin(any())).thenReturn(None)
    when(userDaoMock.findByEmail(any())).thenReturn(None)

    val newUserAdderMock = mock[NewUserAdder]

    // When
    val result = new RegisterService(userDaoMock, newUserAdderMock).register("Adamw", "Adam@example.org", "123456")

    // Then
    result should be ('right)

    val userCaptor = ArgumentCaptor.forClass(classOf[User])
    verify(newUserAdderMock).add(userCaptor.capture())
    val user = userCaptor.getValue

    user.authentication.username should be ("Adamw")
    user.authentication.usernameLowerCase should be ("adamw")
    user.email should be ("adam@example.org")
    user.avatarUrl should be (User.defaultAvatarUrl("adam@example.org"))
    user.token.length should be > (0)
    Authentication.passwordsMatch("123456", user.authentication) should be (true)
  }

  it should "not register a user if a user with the same login already exists" in {
    // Given
    val userDaoMock = mock[UserDAO]
    when(userDaoMock.findByLowerCasedLogin(any())).thenReturn(Some(mock[User]))
    when(userDaoMock.findByEmail(any())).thenReturn(None)

    // When
    val result = new RegisterService(userDaoMock, null).register("adamw", "adam@example.org", "123456")

    // Then
    result should be ('left)
  }

  it should "not register a user if a user with the same email already exists" in {
    // Given
    val userDaoMock = mock[UserDAO]
    when(userDaoMock.findByLowerCasedLogin(any())).thenReturn(None)
    when(userDaoMock.findByEmail(any())).thenReturn(Some(mock[User]))

    // When
    val result = new RegisterService(userDaoMock, null).register("adamw", "adam@example.org", "123456")

    // Then
    result should be ('left)
  }
}
