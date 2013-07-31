package com.softwaremill.codebrag

import akka.actor.Props
import com.softwaremill.codebrag.common.Event
import com.softwaremill.codebrag.service.actors.ActorSystemSupport
import com.softwaremill.codebrag.service.commits.CommitReviewTaskGenerator
import com.softwaremill.codebrag.dao._
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.service.followups.FollowupsGenerator
import com.softwaremill.codebrag.service.events.EventLogger
import com.softwaremill.codebrag.domain.CommitsUpdatedEvent
import com.softwaremill.codebrag.domain.reactions.LikeEvent
import com.softwaremill.codebrag.domain.reactions.UnlikeEvent

trait EventingConfiguration extends ActorSystemSupport {

  def userDao: UserDAO
  def commitReviewTaskDao: CommitReviewTaskDAO
  def commitInfoDao: CommitInfoDAO
  def followupDao: FollowupDAO
  def followupWithReactionsDao: FollowupWithReactionsDAO

  val eventLogger = actorSystem.actorOf(Props(classOf[EventLogger]))
  val reviewTaskGeneratorActor = actorSystem.actorOf(Props(new CommitReviewTaskGenerator(userDao, commitReviewTaskDao, commitInfoDao)))
  val followupGeneratorActor = actorSystem.actorOf(Props(new FollowupsGenerator(followupDao, userDao, commitInfoDao, followupWithReactionsDao: FollowupWithReactionsDAO)))

  actorSystem.eventStream.subscribe(eventLogger, classOf[Event])
  actorSystem.eventStream.subscribe(reviewTaskGeneratorActor, classOf[CommitsUpdatedEvent])
  actorSystem.eventStream.subscribe(reviewTaskGeneratorActor, classOf[NewUserRegistered])
  actorSystem.eventStream.subscribe(followupGeneratorActor, classOf[LikeEvent])
  actorSystem.eventStream.subscribe(followupGeneratorActor, classOf[UnlikeEvent])
}
