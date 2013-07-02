package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config

trait EmailConfig {
  def rootConfig: Config

  private lazy val emailConfig = rootConfig.getConfig("email")

  lazy val emailSmtpHost: String = emailConfig.getString("smtp-host")
  lazy val emailSmtpPort: String = emailConfig.getString("smtp-port")
  lazy val emailSmtpUserName: String = emailConfig.getString("smtp-username")
  lazy val emailSmtpPassword: String = emailConfig.getString("smtp-password")
  lazy val emailFrom: String = emailConfig.getString("from")
  lazy val emailTaskSQSQueue: String = emailConfig.getString("sqs-task-queue")
  lazy val emailEncoding: String = emailConfig.getString("encoding")
}
