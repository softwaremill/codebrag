package com.softwaremill.codebrag

import akka.actor.Props
import com.softwaremill.codebrag.service.github.jgit.DebugEventLogger
import com.softwaremill.codebrag.common.Event
import com.softwaremill.codebrag.service.actors.ActorSystemSupport
import com.softwaremill.codebrag.service.github.CommitReviewTaskGenerator
import com.softwaremill.codebrag.dao.{UserDAO, CommitReviewTaskDAO}
import com.softwaremill.codebrag.domain.CommitsUpdatedEvent

trait EventingConfiguration extends ActorSystemSupport {

  val userDao: UserDAO
  val commitReviewTaskDao: CommitReviewTaskDAO

  val debugLogger = actorSystem.actorOf(Props(classOf[DebugEventLogger]))
  val reviewTaskGenerator = actorSystem.actorOf(Props(new CommitReviewTaskGenerator(userDao, commitReviewTaskDao)))

  actorSystem.eventStream.subscribe(debugLogger, classOf[Event])
  actorSystem.eventStream.subscribe(reviewTaskGenerator, classOf[CommitsUpdatedEvent])
}
