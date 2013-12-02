package com.softwaremill.codebrag.common

import org.bson.types.ObjectId
import org.joda.time.DateTime

/**
 * Describes EventBus based event with required/optional data
 *
 * @see com.softwaremill.codebrag.common.EventBus
 */
trait Event

/**
 * Trait for events collected for stats
 */
trait StatisticEvent extends Event {

  def timestamp: DateTime

  def eventType: String

  def userId: Option[ObjectId]

  def toEventStream: String

}