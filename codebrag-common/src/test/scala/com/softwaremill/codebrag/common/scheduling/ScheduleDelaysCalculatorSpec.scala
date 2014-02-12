package com.softwaremill.codebrag.common.scheduling

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.common.{Clock, FixtureTimeClock}
import org.joda.time.{Period, DateTime}
import org.joda.time.format.DateTimeFormat
import scala.concurrent.duration._


class ScheduleDelaysCalculatorSpec extends FlatSpec with MockitoSugar with ShouldMatchers {

  val sendHour = 9
  val sendMinute = 10
  val interval = 1 minute

  it should "calculate initial delay for today if now is before given time" in {
    //given
    val clock = clockAt("12/11/2013 8:45")

    // when
    val delay = ScheduleDelaysCalculator.delayToGivenTimeInMillis(sendHour, sendMinute)(clock)

    // then
    val expectedDelay = delayTo("12/11/2013 9:10")(clock)
    delay should be(expectedDelay)
  }

  it should "calculate initial delay for today if now is after given time" in {
    //given
    val clock = clockAt("12/11/2013 9:11")

    // when
    val delay = ScheduleDelaysCalculator.delayToGivenTimeInMillis(sendHour, sendMinute)(clock)

    // then

    val expectedDelay = delayTo("13/11/2013 9:10")(clock)
    delay should be(expectedDelay)
  }

  it should "calculate initial delay for next day if now is at given time" in {
    //given
    val clock = clockAt("12/11/2013 9:10")

    // when
    val delay = ScheduleDelaysCalculator.delayToGivenTimeInMillis(sendHour, sendMinute)(clock)

    // then

    val expectedDelay = delayTo("13/11/2013 9:10")(clock)
    delay should be(expectedDelay)
  }

  it should "calculate next delay" in {
    // given
    val clock = clockAt("12/11/2013 11:00")

    // when
    val period = Period.millis(interval.toMillis.toInt)
    val delay = ScheduleDelaysCalculator.delayInMillis(period)(clock)

    // then
    val expectedDelay = delayTo("12/11/2013 11:01")(clock)
    delay should be(expectedDelay)
  }


  private def delayTo(string: String) = {
    val expectedNextSendTime = dateTime(string)
    (clock: Clock) => expectedNextSendTime.getMillis - clock.now.getMillis
  }

  private def clockAt(string: String) = {
    val time = dateTime(string)
    new FixtureTimeClock(time.getMillis)
  }

  private def dateTime(string: String) = DateTime.parse(string, DateTimeFormat.forPattern("dd/MM/yyyy HH:mm"))
}
