package com.softwaremill.codebrag.dao

import com.typesafe.config.Config

trait MongoConfig {
  def rootConfig: Config

  lazy val mongoServers: String = rootConfig.getString("mongo.servers")
  lazy val mongoDatabase: String = rootConfig.getString("mongo.database")
}
