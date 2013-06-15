package com.softwaremill.codebrag.service.config

trait EmailConfig {
  lazy val emailSmtpHost: String = ""
  lazy val emailSmtpPort: String = ""
  lazy val emailSmtpUserName: String = ""
  lazy val emailSmtpPassword: String = ""
  lazy val emailFrom: String = ""
  lazy val emailTaskSQSQueue: String = ""
  lazy val emailEncoding: String = "UTF-8"
}
