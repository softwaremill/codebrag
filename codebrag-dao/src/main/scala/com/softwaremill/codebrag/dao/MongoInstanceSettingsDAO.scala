package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.InstanceSettings

class MongoInstanceSettingsDAO extends InstanceSettingsDAO {

  import com.foursquare.rogue.LiftRogue._

  def readOrCreate = {
    InstanceSettingsRecord.count match {
      case 0 => Right(createInstance)
      case 1 => Right(readInstance)
      case _ => Left(s"More than one record exists in collection '${InstanceSettingsRecord.collectionName}'!")
    }
  }

  private def readInstance = {
    val record = InstanceSettingsRecord
      .findAll
      .head
    InstanceSettings(record.id.toString())
  }

  private def createInstance = {
    InstanceSettingsRecord.upsert(
      InstanceSettingsRecord.limit(1).asDBObject,
      InstanceSettingsRecord.createRecord.asDBObject)
    readInstance
  }

}
