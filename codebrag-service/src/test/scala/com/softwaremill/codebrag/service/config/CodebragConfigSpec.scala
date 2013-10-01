package com.softwaremill.codebrag.service.config

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config.ConfigFactory
import org.joda.time.{Days, Minutes, Hours}

class CodebragConfigSpec extends FlatSpec with ShouldMatchers {

  class TestConfig(configStr: String) extends CodebragConfig {
    def rootConfig = {
      ConfigFactory.parseString(configStr)
    }
  }

  it should "transform hours into period" in {
    val config = new TestConfig(expirationTime(10, "H"))
    config.invitationExpiryTime should be(Hours.hours(10))
  }

  it should "transform minutes into period" in {
    val config = new TestConfig(expirationTime(10, "M"))
    config.invitationExpiryTime should be(Minutes.minutes(10))
  }

  it should "transform days into period" in {
    val config = new TestConfig(expirationTime(10, "D"))
    config.invitationExpiryTime should be(Days.days(10))
  }

  it should "transform into period when multiple spaces between amount and unit" in {
    val configStr = "codebrag { invitation-expiry-time = \"10     D\" }"
    val config = new TestConfig(configStr)
    config.invitationExpiryTime should be(Days.days(10))
  }

  it should "transform into period when no spaces between amount and unit" in {
    val configStr = "codebrag { invitation-expiry-time = \"10D\" }"
    val config = new TestConfig(configStr)
    config.invitationExpiryTime should be(Days.days(10))
  }

  def expirationTime(amount: Int, unit: String) = {
    val param = amount + " " + unit
    "codebrag { invitation-expiry-time = \"" + param  + "\" }"
  }
}
