package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.dao.{CommitInfoDAO, CommitReviewTaskDAO}
import com.softwaremill.codebrag.domain.CommitsUpdatedEvent
import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO

class CommitReviewTaskGenerator(val userDao: UserDAO, val commitToReviewDao: CommitReviewTaskDAO, val commitInfoDao: CommitInfoDAO) extends Actor with Logging
with CommitReviewTaskGeneratorActions {

  def receive = {
    case (event: CommitsUpdatedEvent) => handleCommitsUpdated(event)
  }

}