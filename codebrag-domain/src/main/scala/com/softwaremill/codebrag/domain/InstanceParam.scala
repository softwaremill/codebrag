package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class InstanceParam(key: String, value: String)

case class InstanceId(value: String) {
  def toInstanceParam = InstanceParam(InstanceId.Key, value)
  def creationTime = new ObjectId(value).getTime
}
object InstanceId {
  val Key = "ID"

  def createNew = InstanceId((new ObjectId).toString)
}



