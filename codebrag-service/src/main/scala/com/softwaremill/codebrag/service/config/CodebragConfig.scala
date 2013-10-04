package com.softwaremill.codebrag.service.config

import org.joda.time._
import scala.concurrent.duration.FiniteDuration

trait CodebragConfig extends ConfigWithDefault {

  import TimeUnit._

  lazy val localGitStoragePath: String = rootConfig.getString("codebrag.local-git-storage-path")
  lazy val debugServicesLogin: String = rootConfig.getString("codebrag.debug-services-login")
  lazy val debugServicesPassword: String = rootConfig.getString("codebrag.debug-services-password")
  lazy val demo: Boolean = getBoolean("codebrag.demo", default = false)
  lazy val applicationUrl: String = rootConfig.getString("codebrag.application-url")

  lazy val invitationExpiryTime: ReadablePeriod = {
    val key = "codebrag.invitation-expiry-time"
    val pattern = getTimePattern(key, "24 H")

    pattern.unit match {
      case Hours => org.joda.time.Hours.hours(pattern.time)
      case Minutes => org.joda.time.Minutes.minutes(pattern.time)
      case Days => org.joda.time.Days.days(pattern.time)
      case _ => throw new RuntimeException(s"Incorrect invitation expiry time. Check $key configuration variable")
    }
  }

  lazy val userNotifications: Boolean = getBoolean("codebrag.user-notifications", default = true)

  lazy val notificationsCheckInterval: FiniteDuration = {
    import scala.concurrent.duration._
    val key = "codebrag.notifications-check-interval"
    val pattern = getTimePattern(key, "15 M")

    pattern.unit match {
      case Hours => pattern.time.hours
      case Minutes => pattern.time.minutes
      case Seconds => pattern.time.seconds
      case _ => throw new RuntimeException(s"Incorrect invitation expiry time. Check $key configuration variable")
    }
  }

  private def getTimePattern(key: String, default: String): TimePattern = {
    val expirationString = getString(key, default).trim
    val timeUnit = expirationString.takeRight(1)
    val amount = expirationString.dropRight(1).trim.toInt
    TimePattern(amount, TimeUnit.withName(timeUnit))
  }

  private case class TimePattern(time: Int, unit: TimeUnit)

}

private object TimeUnit extends Enumeration {
  type TimeUnit = Value

  val Days = Value("D")
  val Hours = Value("H")
  val Minutes = Value("M")
  val Seconds = Value("S")
}


