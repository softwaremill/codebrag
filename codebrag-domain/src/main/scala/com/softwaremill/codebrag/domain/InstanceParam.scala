package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class InstanceParam(key: String, value: String)

case class LicenceKey(value: String) {
  def toInstanceParam = InstanceParam(LicenceKey.Key, value)
}
object LicenceKey {
  val Key = "LICENCE"
}

case class InstanceId(value: String) {
  def toInstanceParam = InstanceParam(InstanceId.Key, value)
}
object InstanceId {
  val Key = "ID"

  def createNew = InstanceId((new ObjectId).toString)
}



