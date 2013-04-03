package com.softwaremill.codebrag.domain

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class AuthenticationSpec extends FlatSpec with ShouldMatchers {
  behavior of "Authentication password matcher"

  it should "return false when passwords don't match" in {
    val matches = Authentication.passwordsMatch("1", Authentication.basic("p", "a"))
    matches should be (false)
  }

  it should "return true when passwords match" in {
    val matches = Authentication.passwordsMatch("1", Authentication.basic("p", "1"))
    matches should be (true)
  }
}
