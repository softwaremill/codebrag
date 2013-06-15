package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config

trait AwsConfig {
  def rootConfig: Config

  lazy val awsAccessKeyId: String = rootConfig.getString("aws.access-key-id")
  lazy val awsSecretAccessKey: String = rootConfig.getString("aws.secret-access-key")
}
