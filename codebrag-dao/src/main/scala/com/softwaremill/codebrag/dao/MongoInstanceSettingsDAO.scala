package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.InstanceSettings

class MongoInstanceSettingsDAO extends InstanceSettingsDAO {

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
    val record = InstanceSettingsRecord
      .createRecord
      .save
    InstanceSettings(record.id.toString())
  }

}
