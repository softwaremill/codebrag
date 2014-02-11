package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.dao.{CommitReviewTaskDAO}
import com.softwaremill.codebrag.domain.CommitsUpdatedEvent
import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO

class CommitReviewTaskGenerator(val userDao: UserDAO, val commitToReviewDao: CommitReviewTaskDAO, val commitInfoDao: CommitInfoDAO) extends Actor with Logging
with CommitReviewTaskGeneratorActions {

  def receive = {
    case (event: CommitsUpdatedEvent) => handleCommitsUpdated(event)
  }

}