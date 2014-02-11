package com.softwaremill.codebrag.common.scheduling

import com.softwaremill.codebrag.common.Clock
import org.joda.time.{DateTime, Period}
import scala.concurrent.duration.Duration

/**
 * Calculates amount of time left to given moment
 */
object ScheduleDelaysCalculator {

  def delayToGivenTimeInMillis(sendHour: Int, sendMinute: Int)(implicit clock: Clock) = {
    val sendPeriod = new Period().withHours(sendHour).withMinutes(sendMinute)

    val day = if(isBeforeSendHourToday(clock, sendPeriod)) {
      clock.now.withTimeAtStartOfDay().plus(sendPeriod)
    } else {
      clock.now.plusDays(1).withTimeAtStartOfDay().plus(sendPeriod)
    }
    day.getMillis - clock.now.getMillis
  }

  def delayInMillis(delayPeriod: Period)(implicit clock: Clock) = {
    val nextSend = clock.now.plus(delayPeriod)
    nextSend.getMillis - clock.now.getMillis
  }

  def dateAtDelay(now: DateTime, duration: Duration) = now.plusMillis(duration.toMillis.toInt)

  private def isBeforeSendHourToday(clock: Clock, period: Period) = {
    val sendTime = clock.now.withTimeAtStartOfDay.plus(period)
    clock.now.isBefore(sendTime)
  }

}