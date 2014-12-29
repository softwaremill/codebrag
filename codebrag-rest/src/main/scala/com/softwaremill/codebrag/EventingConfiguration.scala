package com.softwaremill.codebrag

import akka.actor.Props
import com.softwaremill.codebrag.common.{EventBus, Event, StatisticEvent}
import com.softwaremill.codebrag.service.actors.ActorSystemSupport
import com.softwaremill.codebrag.dao.events.EventDAO
import com.softwaremill.codebrag.service.followups.FollowupsGenerator
import com.softwaremill.codebrag.domain.reactions.LikeEvent
import com.softwaremill.codebrag.domain.reactions.UnlikeEvent
import com.softwaremill.codebrag.eventstream.StatisticEventsCollector
import com.softwaremill.codebrag.service.events.EventLogger
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.followup.{FollowupWithReactionsDAO, FollowupDAO}

trait EventingConfiguration extends ActorSystemSupport {

  def userDao: UserDAO
  def commitInfoDao: CommitInfoDAO
  def followupDao: FollowupDAO
  def followupWithReactionsDao: FollowupWithReactionsDAO
  def eventDao: EventDAO
  def eventBus: EventBus

  lazy val eventLogger = actorSystem.actorOf(Props(classOf[EventLogger]))
  lazy val followupGeneratorActor = actorSystem.actorOf(Props(new FollowupsGenerator(followupDao, userDao, commitInfoDao, followupWithReactionsDao: FollowupWithReactionsDAO, eventBus)))
  lazy val statsEventsCollector = actorSystem.actorOf(Props(new StatisticEventsCollector(eventDao)))

  def setupEvents() {
    actorSystem.eventStream.subscribe(eventLogger, classOf[Event])
    actorSystem.eventStream.subscribe(followupGeneratorActor, classOf[LikeEvent])
    actorSystem.eventStream.subscribe(followupGeneratorActor, classOf[UnlikeEvent])
    actorSystem.eventStream.subscribe(statsEventsCollector, classOf[StatisticEvent])
  }
}
