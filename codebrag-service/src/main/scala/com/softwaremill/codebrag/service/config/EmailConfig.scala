package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config
import com.softwaremill.codebrag.common.config.ConfigWithDefault

trait EmailConfig extends ConfigWithDefault {
  def rootConfig: Config

  private lazy val emailConfig = rootConfig.getConfig("email")

  lazy val emailSmtpHost: String = emailConfig.getString("smtp-host")
  lazy val emailSmtpPort: String = emailConfig.getString("smtp-port")
  lazy val emailSmtpUserName: String = emailConfig.getString("smtp-username")
  lazy val emailSmtpPassword: String = emailConfig.getString("smtp-password")
  lazy val emailFrom: String = emailConfig.getString("from")
  lazy val emailEncoding: String = emailConfig.getString("encoding")
  lazy val sslConnection: Boolean = emailConfig.getBoolean("ssl-connection")
  lazy val verifySSLCertificate: Boolean = getBoolean("email.verify-ssl-certificate", true)
}
