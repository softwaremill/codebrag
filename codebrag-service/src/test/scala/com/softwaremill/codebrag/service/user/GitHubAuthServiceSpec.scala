package com.softwaremill.codebrag.service.user

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.eclipse.egit.github.core.User
import com.softwaremill.codebrag.service.config.GithubConfig
import org.scalatest.matchers.ShouldMatchers

class GitHubAuthServiceSpec extends FlatSpec with MockitoSugar with ShouldMatchers {
  behavior of "GitHub Auth Service"

  val testConfig = new GithubConfig {
    def rootConfig = ???
  }

  it should "read email of user" in {
    // given
    val email = "my@email.com"
    val service = new GitHubAuthService(testConfig)
    val user = new User()
    user.setEmail(email)

    // when
    val userEmail = service.readEmail(user)

    // then
    userEmail should be(email)

  }

  it should "fallback to empty email if user doesn't have public email" in {
    // given
    val service = new GitHubAuthService(testConfig)
    val user = new User()
    user.setEmail(null)

    // when
    val userEmail = service.readEmail(user)

    // then
    userEmail should be('empty)
  }
}
