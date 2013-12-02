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
      clock.currentDateTime.withTimeAtStartOfDay().plus(sendPeriod)
    } else {
      clock.currentDateTime.plusDays(1).withTimeAtStartOfDay().plus(sendPeriod)
    }
    day.getMillis - clock.currentDateTime.getMillis
  }

  def delayInMillis(delayPeriod: Period)(implicit clock: Clock) = {
    val nextSend = clock.currentDateTime.plus(delayPeriod)
    nextSend.getMillis - clock.currentDateTime.getMillis
  }

  def dateAtDelay(now: DateTime, duration: Duration) = now.plusMillis(duration.toMillis.toInt)

  private def isBeforeSendHourToday(clock: Clock, period: Period) = {
    val sendTime = clock.currentDateTime.withTimeAtStartOfDay.plus(period)
    clock.currentDateTime.isBefore(sendTime)
  }

}