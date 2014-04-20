package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

/**
 * Class contains Codebrag's instance global settings, i.e. uniqueId
 */
case class InstanceSettings(uniqueId: String) {
  def uniqueIdAsObjectId = new ObjectId(uniqueId)
}
