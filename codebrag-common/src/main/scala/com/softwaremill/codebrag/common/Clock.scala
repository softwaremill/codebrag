package com.softwaremill.codebrag.common

import org.joda.time.{DateTimeZone, DateTime}

trait Clock {
  def currentDateTime: DateTime
  def currentDateTimeUTC: DateTime
  def currentTimeMillis: Long
}

object RealTimeClock extends Clock {
  def currentDateTime = DateTime.now()
  def currentDateTimeUTC = DateTime.now(DateTimeZone.UTC)
  def currentTimeMillis = System.currentTimeMillis()
}

class FixtureTimeClock(millis: Long) extends Clock {
  def currentDateTime = new DateTime(millis)
  def currentDateTimeUTC = new DateTime(millis, DateTimeZone.UTC)
  def currentTimeMillis = millis
}

/**
 * Simple trait to be used in tests required implicit clock value
 */
trait ClockSpec {

  implicit lazy val clock = new FixtureTimeClock(fixtureTime)

  def fixtureTime = System.currentTimeMillis()

}
