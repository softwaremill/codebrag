package com.softwaremill.codebrag.service.notification

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.service.config.{CodebragConfig, ConfigWithDefault}
import com.typesafe.config.ConfigFactory
import collection.JavaConversions._
import com.softwaremill.codebrag.common.{Clock, FixtureTimeClock}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


class DailyDigestScheduleCalculatorSpec extends FlatSpec with MockitoSugar with ShouldMatchers {

  val config = new CodebragConfig with ConfigWithDefault {
    val params = Map(
      "codebrag.user-email-notifications.daily-digest-hour" -> "9",
      "codebrag.user-email-notifications.daily-digest-minute" -> "10",
      "codebrag.user-email-notifications.daily-digest-interval" -> "1 minute"
    )
    def rootConfig = ConfigFactory.parseMap(params)
  }

  it should "calculate initial send date for today if now is before send time" in {
    //given
    val clock = clockAt("12/11/2013 8:45")

    // when
    val delay = DailyDigestScheduleCalculator.initialScheduleTimeDelayInMillis(config)(clock)

    // then
    val expectedDelay = delayTo("12/11/2013 9:10")(clock)
    delay should be(expectedDelay)
  }

  it should "calculate initial send date for today if now is after send time" in {
    //given
    val clock = clockAt("12/11/2013 9:11")

    // when
    val delay = DailyDigestScheduleCalculator.initialScheduleTimeDelayInMillis(config)(clock)

    // then
    
    val expectedDelay = delayTo("13/11/2013 9:10")(clock)
    delay should be(expectedDelay)
  }

  it should "calculate initial send date for next day if now is at send time" in {
    //given
    val clock = clockAt("12/11/2013 9:10")

    // when
    val delay = DailyDigestScheduleCalculator.initialScheduleTimeDelayInMillis(config)(clock)

    // then

    val expectedDelay = delayTo("13/11/2013 9:10")(clock)
    delay should be(expectedDelay)
  }

  it should "calculate next send delay" in {
    // given
    val clock = clockAt("12/11/2013 11:00")

    // when
    val delay = DailyDigestScheduleCalculator.nextScheduleTimeDelayInMillis(config)(clock)

    // then
    val expectedDelay = delayTo("12/11/2013 11:01")(clock)
    delay should be(expectedDelay)
  }

  
  private def delayTo(string: String) = {
    val expectedNextSendTime = dateTime(string)
    (clock: Clock) => expectedNextSendTime.getMillis - clock.currentDateTime.getMillis
  }

  private def clockAt(string: String) = {
    val time = dateTime(string)
    new FixtureTimeClock(time.getMillis)
  }
  
  private def dateTime(string: String) = DateTime.parse(string, DateTimeFormat.forPattern("dd/MM/yyyy HH:mm")) 
}
