package com.softwaremill.codebrag.service.github

import com.softwaremill.codebrag.dao.{CommitInfoDAO, CommitReviewTaskDAO, UserDAO}
import com.softwaremill.codebrag.domain.CommitsUpdatedEvent
import akka.actor.Actor
import pl.softwaremill.common.util.time.Clock
import com.typesafe.scalalogging.slf4j.Logging

class CommitReviewTaskGenerator(val userDao: UserDAO, val commitToReviewDao: CommitReviewTaskDAO, val commitInfoDao: CommitInfoDAO, val clock: Clock) extends Actor with Logging
with CommitReviewTaskGeneratorActions {

  def receive = {
    case (event: CommitsUpdatedEvent) => handleCommitsUpdated(event)
  }

}