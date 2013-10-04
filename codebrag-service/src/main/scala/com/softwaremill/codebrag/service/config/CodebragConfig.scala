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

  lazy val userNotifications: Boolean = getBoolean("codebrag.user-notifications", default = true)

  lazy val notificationsCheckInterval = getMilliseconds("codebrag.notifications-check-interval", 15.minutes.toMillis).millis

  lazy val userOfflinePeriod = Period.millis(getMilliseconds("codebrag.user-offline-period", 5.minutes.toMillis).toInt)

}

