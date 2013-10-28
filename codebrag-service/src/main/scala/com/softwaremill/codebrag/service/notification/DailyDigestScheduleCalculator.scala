package com.softwaremill.codebrag.service.notification

import com.softwaremill.codebrag.service.config.CodebragConfig
import com.softwaremill.codebrag.common.Clock
import org.joda.time.Period

object DailyDigestScheduleCalculator {

  def initialScheduleTimeDelayInMillis(config: CodebragConfig)(implicit clock: Clock) = {
    val sendHour = config.dailyDigestSendHour
    val sendMinute = config.dailyDigestSendMinute
    val sendPeriod = new Period().withHours(sendHour).withMinutes(sendMinute)

    val day = if(isBeforeSendHourToday(clock, sendPeriod)) {
      clock.currentDateTime.withTimeAtStartOfDay().plus(sendPeriod)
    } else {
      clock.currentDateTime.plusDays(1).withTimeAtStartOfDay().plus(sendPeriod)
    }
    day.getMillis - clock.currentDateTime.getMillis
  }

  def nextScheduleTimeDelayInMillis(config: CodebragConfig)(implicit clock: Clock) = {
    val interval = config.dailyDigestSendInterval
    val nextSend = clock.currentDateTime.plus(interval)
    nextSend.getMillis - clock.currentDateTime.getMillis
  }

  private def isBeforeSendHourToday(clock: Clock, period: Period) = {
    val sendTime = clock.currentDateTime.withTimeAtStartOfDay.plus(period)
    clock.currentDateTime.isBefore(sendTime)
  }

}
