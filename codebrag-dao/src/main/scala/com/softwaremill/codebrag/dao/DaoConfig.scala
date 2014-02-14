package com.softwaremill.codebrag.dao

import com.typesafe.config.Config
import scala.Predef._
import com.typesafe.scalalogging.slf4j.Logging

trait DaoConfig extends Logging {
  def rootConfig: Config

  object StorageType extends Enumeration {
    type StorageType = Value
    val Embedded, Mongo = Value
  }
  import StorageType._

  lazy val storageType: StorageType = {
    if (rootConfig.hasPath("storage.type")) {
      rootConfig.getString("storage.type").toLowerCase.trim match {
        case "embedded" => Embedded
        case "mongo" => Mongo
        case x => throw new IllegalStateException(s"Unknown storage type $x")
      }
    } else {
      logger.info("Assuming default storage: mongo")
      Mongo
    }
  }

  lazy val mongoServers: String = rootConfig.getString("mongo.servers")
  lazy val mongoDatabase: String = rootConfig.getString("mongo.database")

  lazy val embeddedDataDir: String = rootConfig.getString("storage.embedded.datadir")
}
