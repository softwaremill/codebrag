package com.softwaremill.codebrag.service.github

import org.scalatest.{GivenWhenThen, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.eclipse.egit.github.core.User
import org.mockito.Mockito._
import org.eclipse.egit.github.core.service.UserService
import org.mockito.Mockito

class GitHubAuthServiceSpec extends FlatSpec with MockitoSugar with GivenWhenThen {
  behavior of ("GitHub Auth Service")

  it should "read email of user" in {
    Given("auth service")
    val service = new GitHubAuthService
    And("loaded user data")
    val user = mock[User]
    Mockito.when(user.getEmail).thenReturn("some@email.com")

    When("Extracting user email")
    service.readEmail(user, null)

    Then("user's public email is read")
    verify(user, times(2)).getEmail
  }

  it should "fetch list of emails if user doesn't have public email" in {
    import scala.collection.JavaConversions._

    Given("auth service")
    val service = new GitHubAuthService
    And("user service")
    val userService = mock[UserService]
    val user = mock[User]
    Mockito.when(userService.getEmails).thenReturn(List("email@email.com"))

    When("extracting user data")
    service.readEmail(user, userService)

    Then("service is used to look up users private emails")
  }
}
