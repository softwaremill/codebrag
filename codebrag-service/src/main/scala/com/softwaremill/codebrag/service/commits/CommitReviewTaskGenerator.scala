package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.domain.NewCommitsLoadedEvent
import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.reviewtask.CommitReviewTaskDAO

class CommitReviewTaskGenerator(val userDao: UserDAO, val commitToReviewDao: CommitReviewTaskDAO, val commitInfoDao: CommitInfoDAO) extends Actor with Logging
with CommitReviewTaskGeneratorActions {

  def receive = {
    case (event: NewCommitsLoadedEvent) => handleCommitsUpdated(event)
  }

}