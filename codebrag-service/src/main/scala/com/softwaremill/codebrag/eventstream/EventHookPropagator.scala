package com.softwaremill.codebrag.eventstream

import com.softwaremill.codebrag.common.{Hookable, Clock}
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain._
import com.softwaremill.codebrag.domain.reactions._

import akka.actor.Actor
import com.ning.http.client.StringPart
import akka.dispatch.ExecutionContexts.global
import com.typesafe.scalalogging.slf4j.Logging
import dispatch._
import org.json4s.jackson.Serialization.{write => jsonWrite}

class EventHookPropagator(
    hookListeners: Map[String, List[String]],
    commitInfoDao: CommitInfoDAO,
    userDao: UserDAO
  )(implicit clock: Clock) extends Actor with Logging {

  implicit val formats = InternalSerializers.all

  def receive = {
    case (event: LikeEvent) if hasListeners(event) =>
      val commitInfo = commitInfoDao.findByCommitId(event.like.commitId)
      val user = userDao.findById(event.userId)
      notifyListeners(LikeHook(commitInfo, user, event.like, event.hookName))

    case (event: UnlikeEvent) if hasListeners(event) =>
      val commitInfo = commitInfoDao.findByCommitId(event.like.commitId)
      val user = event.userId  match {
        case Some(userId) => userDao.findById(userId)
        case _ => None
      }
      notifyListeners(UnlikeHook(commitInfo, user, event.like, event.hookName))

    case (event: CommentAddedEvent) if hasListeners(event) =>
      val commitInfo = commitInfoDao.findByCommitId(event.comment.commitId)
      val user = userDao.findById(event.userId)
      notifyListeners(CommentAddedHook(commitInfo, user, event.comment, event.hookName))

    case (event: CommitReviewedEvent) if hasListeners(event) =>
      val user = userDao.findById(event.userId)
      notifyListeners(CommitReviewedHook(event.commit, user, event.hookName))

    case (event: NewCommitsLoadedEvent) if hasListeners(event) =>
      val user = event.userId match {
        case Some(userId) => userDao.findById(userId)
        case _ => None
      }
      notifyListeners(NewCommitsLoadedHook(user, event.repoName, event.currentSHA, event.newCommits, event.hookName))

    case (event: NewUserRegistered) if hasListeners(event) =>
      val user = userDao.findById(event.userId)
      notifyListeners(NewUserRegisteredHook(user, event.login, event.fullName, event.hookName))

  }

  private def notifyListeners(hook: Hook) = {
    val hookName = hook.hookName
    logger.debug(s"Sending $hookName to subscribers...")

    hookListeners.get(hookName) match {

      case Some(hookUrls) =>
        val json = jsonWrite(hook)

        hookUrls.foreach { hookUrl =>
          val request = url(hookUrl).POST.setHeader("Content-Type", "application/json; charset=UTF-8") << json

          Http(request OK as.String).onComplete( status =>
            logger.debug(s"Got response: $status from $hookUrl")
          )
        }

      case _ =>
        logger.debug(s"No listeners defined for $hookName")

    }
  }

  private def hasListeners(event: Hookable) = {
    hookListeners.get(event.hookName).isDefined
  }

}
