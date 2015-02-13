package com.softwaremill.codebrag.eventstream

import akka.actor.Actor
import com.ning.http.client.StringPart
import com.softwaremill.codebrag.common.Hookable
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain._
import com.softwaremill.codebrag.domain.reactions._

import com.typesafe.scalalogging.slf4j.Logging
import dispatch._
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.FieldSerializer._
import org.json4s.ext.JodaTimeSerializers
import org.json4s.jackson.Serialization.{write => jsonWrite}
import org.json4s.mongo._
import org.json4s.{DefaultFormats, FieldSerializer}

sealed trait Hook {
  def event: Hookable
  val hookDate: DateTime = DateTime.now(DateTimeZone.UTC)
}

case class LikeHook(commitInfo: Option[CommitInfo], override val event: Hookable) extends Hook
case class UnlikeHook(commitInfo: Option[CommitInfo], override val event: Hookable) extends Hook
case class CommentAddedHook(commitInfo: Option[CommitInfo], user: Option[User], override val event: Hookable) extends Hook
case class CommitReviewedHook(user: Option[User], override val event: Hookable) extends Hook
case class NewCommitsLoadedHook(user: Option[User], override val event: Hookable) extends Hook
case class NewUserRegisteredHook(user: Option[User], override val event: Hookable) extends Hook

class EventHookPropagator(
    hookListeners: Map[String, List[String]],
    commitInfoDao: CommitInfoDAO,
    userDao: UserDAO
  ) extends Actor with Logging {

  val LikeEventSerializer = FieldSerializer[LikeEvent](ignore("clock"))
  val UnlikeEventSerializer = FieldSerializer[UnlikeEvent](ignore("clock"))

  implicit val formats =
    DefaultFormats +
    new ObjectIdSerializer() +
    LikeEventSerializer +
    UnlikeEventSerializer ++
    JodaTimeSerializers.all

  def receive = {
    case (event: LikeEvent) =>
      val commitInfo = commitInfoDao.findByCommitId(event.like.commitId)
      notifyListeners(LikeHook(commitInfo, event))

    case (event: UnlikeEvent) =>
      val commitInfo = commitInfoDao.findByCommitId(event.like.commitId)
      notifyListeners(UnlikeHook(commitInfo, event))

    case (event: CommentAddedEvent) =>
      val commitInfo = commitInfoDao.findByCommitId(event.comment.commitId)
      val user = userDao.findById(event.userId)
      notifyListeners(CommentAddedHook(commitInfo, user, event))

    case (event: CommitReviewedEvent) =>
      val user = userDao.findById(event.userId)
      notifyListeners(CommitReviewedHook(user, event))

    case (event: NewCommitsLoadedEvent) =>
      val user = event.userId match {
        case Some(userId) => userDao.findById(userId)
        case _ => None
      }
      notifyListeners(NewCommitsLoadedHook(user, event))

    case (event: NewUserRegistered) =>
      val user = userDao.findById(event.userId)
      notifyListeners(NewUserRegisteredHook(user, event))

  }

  private def notifyListeners(hook: Hook) = {
    val hookName = hook.event.hookName

    logger.debug(s"Sending $hookName to subscribers...")

    for (hookUrl <- hookListeners(hookName)) {
      logger.debug(s"Sending $hookName to $hookUrl")

      val json = jsonWrite(hook)
      val body = new StringPart(hookName, json)
      val request = url(hookUrl).POST.setHeader("Content-Type", "application/json; charset=UTF-8").addBodyPart(body)

      Http(request OK as.String).onComplete( (status) =>
        logger.debug(s"Got response: $status")
      )
    }

  }

}
