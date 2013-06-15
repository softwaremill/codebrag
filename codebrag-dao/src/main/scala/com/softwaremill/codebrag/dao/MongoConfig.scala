package com.softwaremill.codebrag.dao

trait MongoConfig {
  lazy val mongoServers: String = "localhost:27017"
  lazy val mongoDatabase: String = "codebrag"
}
