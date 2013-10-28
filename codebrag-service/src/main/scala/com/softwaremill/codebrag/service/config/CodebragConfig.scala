package com.softwaremill.codebrag.service.config

import org.joda.time._
import scala.concurrent.duration._

trait CodebragConfig extends ConfigWithDefault {

  lazy val localGitStoragePath: String = rootConfig.getString("codebrag.local-git-storage-path")
  lazy val debugServicesLogin: String = rootConfig.getString("codebrag.debug-services-login")
  lazy val debugServicesPassword: String = rootConfig.getString("codebrag.debug-services-password")
  lazy val demo: Boolean = getBoolean("codebrag.demo", default = false)
  lazy val applicationUrl: String = rootConfig.getString("codebrag.application-url")

  lazy val invitationExpiryTime: ReadablePeriod = Period.millis(getMilliseconds("codebrag.invitation-expiry-time", 24.hours.toMillis).toInt)

  lazy val userNotifications: Boolean = getBoolean("codebrag.user-email-notifications.enabled", default = true)

  lazy val notificationsCheckInterval = getMilliseconds("codebrag.user-email-notifications.check-interval", 15.minutes.toMillis).millis

  lazy val userOfflinePeriod = Period.millis(getMilliseconds("codebrag.user-email-notifications.user-offline-after", 5.minutes.toMillis).toInt)

  // Config properties for daily summary send outs
  // Not included in docs and in config template as we prefer not to expose them
  // By default daily summaries are sent at 9am every day
  lazy val dailyDigestSendHour = getInt("codebrag.user-email-notifications.daily-digest-hour", 9)
  lazy val dailyDigestSendMinute = getInt("codebrag.user-email-notifications.daily-digest-minute", 0)
  lazy val dailyDigestSendInterval = Period.millis(getMilliseconds("codebrag.user-email-notifications.daily-digest-interval", 24.hours.toMillis).toInt)
}

