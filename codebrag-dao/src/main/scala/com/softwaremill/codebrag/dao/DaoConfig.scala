package com.softwaremill.codebrag.dao

import com.typesafe.config.Config
import scala.Predef._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.common.config.ConfigWithDefault

trait DaoConfig extends ConfigWithDefault with Logging {
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
      logger.info("Assuming default storage: embedded")
      Embedded
    }
  }

  lazy val isEmbeddedStorage = storageType == StorageType.Embedded

  lazy val mongoServers: String = rootConfig.getString("mongo.servers")
  lazy val mongoDatabase: String = rootConfig.getString("mongo.database")

  lazy val embeddedDataDir: String = getString("codebrag.data-dir", "./data")
  lazy val embeddedBackupHour: Int = getInt("storage.embedded.backup-hour", 5)
}
