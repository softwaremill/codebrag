package com.softwaremill.codebrag

import akka.actor.Props
import com.softwaremill.codebrag.service.github.jgit.DebugEventLogger
import com.softwaremill.codebrag.common.Event
import com.softwaremill.codebrag.service.actors.ActorSystemSupport
import com.softwaremill.codebrag.service.github.CommitReviewTaskGenerator
import com.softwaremill.codebrag.dao.{CommitInfoDAO, UserDAO, CommitReviewTaskDAO}
import com.softwaremill.codebrag.domain.CommitsUpdatedEvent
import pl.softwaremill.common.util.time.Clock
import com.softwaremill.codebrag.service.user.NewUserRegistered

trait EventingConfiguration extends ActorSystemSupport {

  val userDao: UserDAO
  val commitReviewTaskDao: CommitReviewTaskDAO
  val commitInfoDao: CommitInfoDAO
  val clock: Clock

  val debugLogger = actorSystem.actorOf(Props(classOf[DebugEventLogger]))

  val reviewTaskGenerator = actorSystem.actorOf(Props(new CommitReviewTaskGenerator(userDao, commitReviewTaskDao, commitInfoDao, clock)))

  actorSystem.eventStream.subscribe(debugLogger, classOf[Event])
  actorSystem.eventStream.subscribe(reviewTaskGenerator, classOf[CommitsUpdatedEvent])
  actorSystem.eventStream.subscribe(reviewTaskGenerator, classOf[NewUserRegistered])
}
