package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.domain._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.service.commits.branches.{CommitCacheEntry, ReviewedCommitsCache, RepositoryCache}
import CommitAuthorClassification._
import org.joda.time.{DateTimeUtils, DateTime}

class AfterUserRegisteredHook(
  val repoCache: RepositoryCache,
  val reviewedCommitsCache: ReviewedCommitsCache) extends SetStartingDateForUser {

  def run(user: NewUserRegistered) {
    setStartingDate(user)
  }

}

trait SetStartingDateForUser extends Logging {

  val repoCache: RepositoryCache
  val reviewedCommitsCache: ReviewedCommitsCache

  private val LastCommitsToReviewCount = 10
  private val DaysBackToTakeCommits = 30

  protected def setStartingDate(user: NewUserRegistered) {
    val weekBeforeRegistration = user.timestamp.minusDays(DaysBackToTakeCommits)
    val allBranchCommits = repoCache.getBranchCommits(currentBranch)
    val newestForUserToReview = allBranchCommits.filterNot(commitAuthoredByUser(_, user)).take(LastCommitsToReviewCount)
    val dateBoundary = newestForUserToReview.lastOption match {
      case Some(last) => getLatestFromBothDates(last.commitDate, weekBeforeRegistration)
      case None => weekBeforeRegistration
    }
    reviewedCommitsCache.setStartingDateForUser(dateBoundary, user.id)
    logger.debug(s"New user ${user.login} registered. To review boundary date set to ${dateBoundary}")
  }


  def getLatestFromBothDates(lastCommitDate: DateTime, weekBeforeRegistration: DateTime): DateTime = {
    if (lastCommitDate.isBefore(weekBeforeRegistration)) {
      weekBeforeRegistration
    } else {
      lastCommitDate
    }
  }

  private def currentBranch = repoCache.repository.getCheckedOutBranchName

}