package com.softwaremill.codebrag.service.config

import org.joda.time._

trait CodebragConfig extends ConfigWithDefault {

  lazy val localGitStoragePath: String = rootConfig.getString("codebrag.local-git-storage-path")
  lazy val debugServicesLogin: String = rootConfig.getString("codebrag.debug-services-login")
  lazy val debugServicesPassword: String = rootConfig.getString("codebrag.debug-services-password")
  lazy val demo: Boolean = getBoolean("codebrag.demo", default = false)
  lazy val applicationUrl: String = rootConfig.getString("codebrag.application-url")

  lazy val invitationExpiryTime: ReadablePeriod = {

    val expirationString = getString("codebrag.invitation-expiry-time", "24 H").trim
    val timeUnit = expirationString.takeRight(1)
    val amount = expirationString.dropRight(1).trim.toInt

    timeUnit match {
      case "H" => Hours.hours(amount)
      case "M" => Minutes.minutes(amount)
      case "D" => Days.days(amount)
      case _ => throw new RuntimeException("Incorrect invitation expiry time " + expirationString)
    }
  }

  lazy val userNotifications: Boolean = getBoolean("codebrag.user-notifications", default = true)

}


