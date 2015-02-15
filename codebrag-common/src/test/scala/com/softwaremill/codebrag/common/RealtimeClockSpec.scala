package com.softwaremill.codebrag.common

import java.util.Calendar

import org.joda.time.DateTime
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class RealtimeClockSpec extends FlatSpec with ShouldMatchers {

  it should "generate UTC datetime with timezone" in {
    val utc = RealTimeClock.nowUtc
    val now = DateTime.now()

    val timeZone = Calendar.getInstance.getTimeZone
    val offset = timeZone.getOffset(now.toDate.getTime)
    val expected = now.minusMillis(offset)

    utc.getYear should equal(expected.getYear)
    utc.getMonthOfYear should equal(expected.getMonthOfYear)
    utc.getDayOfMonth should equal(expected.getDayOfMonth)
    utc.getHourOfDay should equal(expected.getHourOfDay)
    utc.getMinuteOfDay should equal(expected.getMinuteOfDay)
    utc.getSecondOfDay should equal(expected.getSecondOfDay)
  }

}
