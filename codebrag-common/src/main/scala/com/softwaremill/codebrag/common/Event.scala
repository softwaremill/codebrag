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

  def userId: ObjectId

  def toEventStream: String

}

/**
 * Marks event as hookable which means it can be send to remote listeners
 * check application.config.template how to add listeners
 */
trait Hookable {

  /**
   * This name is used to match hook with list of urls from config
   *
   * @see com.softwaremill.codebrag.eventstream.EventHookPropagator
   */
  def hookName: String

}