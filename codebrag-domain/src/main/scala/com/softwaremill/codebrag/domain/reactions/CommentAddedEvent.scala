package com.softwaremill.codebrag.domain.reactions

import com.softwaremill.codebrag.common._
import com.softwaremill.codebrag.domain.Comment
import org.joda.time.DateTime

/**
 * Describes event when someone added a comment
 */
case class CommentAddedEvent(
    comment: Comment
  )(implicit clock: Clock) extends Event with StatisticEvent with Hookable {

  val hookName = "comment-added"

  def eventType = CommentAddedEvent.EventType

  def timestamp: DateTime = clock.nowUtc

  def userId = comment.authorId

  def toEventStream: String = s"Comment [${comment.message}] from [${comment.authorId}] to [${comment.commitId}}]"

}

object CommentAddedEvent {
  val EventType = "CommentAdded"
}
