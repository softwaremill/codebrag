package com.softwaremill.codebrag.common

import org.bson.types.ObjectId
import org.joda.time.DateTime

/**
 * Describes EventBus based event with required/optional data
 *
 * @see com.softwaremill.codebrag.common.EventBus
 */
trait Event {

  def timestamp: DateTime

  def eventType: String = getClass.getSimpleName

  def userId: Option[ObjectId]

  def toEventStream: String

}
