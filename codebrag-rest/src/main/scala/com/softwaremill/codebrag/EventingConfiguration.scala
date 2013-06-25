package com.softwaremill.codebrag

import akka.actor.Props
import com.softwaremill.codebrag.service.github.jgit.EventLogger
import com.softwaremill.codebrag.common.Event
import com.softwaremill.codebrag.service.actors.ActorSystemSupport
import com.softwaremill.codebrag.service.github.CommitReviewTaskGenerator
import com.softwaremill.codebrag.dao.{FollowupDAO, CommitInfoDAO, UserDAO, CommitReviewTaskDAO}
import com.softwaremill.codebrag.domain.CommitsUpdatedEvent
import pl.softwaremill.common.util.time.Clock
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.service.followups.FollowupsGenerator
import com.softwaremill.codebrag.domain.reactions.CommitLiked

trait EventingConfiguration extends ActorSystemSupport {

  val userDao: UserDAO
  val commitReviewTaskDao: CommitReviewTaskDAO
  val commitInfoDao: CommitInfoDAO
  val followupDao: FollowupDAO

  val debugLogger = actorSystem.actorOf(Props(classOf[EventLogger]))
  val reviewTaskGeneratorActor = actorSystem.actorOf(Props(new CommitReviewTaskGenerator(userDao, commitReviewTaskDao, commitInfoDao)))
  val followupGeneratorActor = actorSystem.actorOf(Props(new FollowupsGenerator(followupDao, userDao, commitInfoDao)))

  actorSystem.eventStream.subscribe(debugLogger, classOf[Event])
  actorSystem.eventStream.subscribe(reviewTaskGeneratorActor, classOf[CommitsUpdatedEvent])
  actorSystem.eventStream.subscribe(reviewTaskGeneratorActor, classOf[NewUserRegistered])
  actorSystem.eventStream.subscribe(followupGeneratorActor, classOf[CommitLiked])
}
