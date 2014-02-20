package com.softwaremill.codebrag

import akka.actor.Props
import com.softwaremill.codebrag.common.{Event, StatisticEvent}
import com.softwaremill.codebrag.service.actors.ActorSystemSupport
import com.softwaremill.codebrag.service.commits.CommitReviewTaskGenerator
import com.softwaremill.codebrag.dao.events.EventDAO
import com.softwaremill.codebrag.service.followups.FollowupsGenerator
import com.softwaremill.codebrag.domain.NewCommitsLoadedEvent
import com.softwaremill.codebrag.domain.reactions.LikeEvent
import com.softwaremill.codebrag.domain.reactions.UnlikeEvent
import com.softwaremill.codebrag.eventstream.StatisticEventsCollector
import com.softwaremill.codebrag.service.events.EventLogger
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskDAO
import com.softwaremill.codebrag.dao.followup.{FollowupWithReactionsDAO, FollowupDAO}
import com.softwaremill.codebrag.dao.repositorystatus.RepositoryStatusDAO

trait EventingConfiguration extends ActorSystemSupport {

  def userDao: UserDAO
  def commitReviewTaskDao: CommitReviewTaskDAO
  def commitInfoDao: CommitInfoDAO
  def followupDao: FollowupDAO
  def followupWithReactionsDao: FollowupWithReactionsDAO
  def repoStatusDao: RepositoryStatusDAO
  def eventDao: EventDAO

  lazy val eventLogger = actorSystem.actorOf(Props(classOf[EventLogger]))
  lazy val reviewTaskGeneratorActor = actorSystem.actorOf(Props(new CommitReviewTaskGenerator(userDao, commitReviewTaskDao, commitInfoDao, repoStatusDao)))
  lazy val followupGeneratorActor = actorSystem.actorOf(Props(new FollowupsGenerator(followupDao, userDao, commitInfoDao, followupWithReactionsDao: FollowupWithReactionsDAO)))
  lazy val statsEventsCollector = actorSystem.actorOf(Props(new StatisticEventsCollector(eventDao)))

  def setupEvents() {
    actorSystem.eventStream.subscribe(eventLogger, classOf[Event])
    actorSystem.eventStream.subscribe(reviewTaskGeneratorActor, classOf[NewCommitsLoadedEvent])
    actorSystem.eventStream.subscribe(followupGeneratorActor, classOf[LikeEvent])
    actorSystem.eventStream.subscribe(followupGeneratorActor, classOf[UnlikeEvent])
    actorSystem.eventStream.subscribe(statsEventsCollector, classOf[StatisticEvent])
  }
}
