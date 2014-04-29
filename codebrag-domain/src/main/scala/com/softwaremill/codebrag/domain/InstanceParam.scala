package com.softwaremill.codebrag.domain

case class InstanceParam(key: String, value: String)

case class LicenceKeyParam(value: String) {
  def toInstanceParam = InstanceParam(LicenceKeyParam.Key, value)
}
object LicenceKeyParam {
  val Key = "LICENCE"
}

case class InstanceIdParam(value: String) {
  def toInstanceParam = InstanceParam(InstanceIdParam.Key, value)
}
object InstanceIdParam {
  val Key = "ID"
}



