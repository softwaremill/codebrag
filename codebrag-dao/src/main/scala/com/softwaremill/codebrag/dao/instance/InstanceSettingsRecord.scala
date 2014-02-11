package com.softwaremill.codebrag.dao.instance

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk

class InstanceSettingsRecord extends MongoRecord[InstanceSettingsRecord] with ObjectIdPk[InstanceSettingsRecord] {

  def meta = InstanceSettingsRecord

}

object InstanceSettingsRecord extends InstanceSettingsRecord with MongoMetaRecord[InstanceSettingsRecord] {

  override def collectionName = "instance_settings"

}
