package com.softwaremill.codebrag.domain.reactions

import com.softwaremill.codebrag.common.{StatisticEvent, Clock, Event}
import com.softwaremill.codebrag.domain.Like
import org.bson.types.ObjectId
import org.joda.time.DateTime

/**
 * Describes event when someone likes given commit
 *
 * @param like case class describing the liked commit
 * @param clock to obtain when event was created
 */
case class LikeEvent(like: Like)(implicit clock: Clock) extends Event with StatisticEvent {

  def eventType = LikeEvent.EventType

  def timestamp: DateTime = clock.nowUtc

  def userId = like.authorId

  def toEventStream: String = s"Commit ${like.commitId} liked by ${like.authorId}"

}

object LikeEvent {
  val EventType = "LikeAdded"
}


/**
 * Describes event when someone dislike given commit
 *
 * @param like case class describing the disliked commit
 * @param clock to obtain when event was created
 */
case class UnlikeEvent(like: Like)(implicit clock: Clock) extends Event {

  def timestamp: DateTime = clock.nowUtc

  def userId: Option[ObjectId] = Some(like.authorId)

  def toEventStream: String = s"User ${like.authorId} doesn't like commit ${like.authorId}"

}
