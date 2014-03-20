package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.domain._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.service.commits.branches.{UserReviewedCommitsCacheEntry, UserReviewedCommitsCache, RepositoryCache}
import CommitAuthorClassification._
import org.joda.time.DateTime

class AfterUserRegisteredHook(
  val repoCache: RepositoryCache,
  val reviewedCommitsCache: UserReviewedCommitsCache) extends SetStartingDateForUser {

  def run(user: NewUserRegistered) {
    setStartingDate(user)
  }

}

trait SetStartingDateForUser extends Logging {

  val repoCache: RepositoryCache
  val reviewedCommitsCache: UserReviewedCommitsCache

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
    val newUserCacheEntry = UserReviewedCommitsCacheEntry.forNewlyRegisteredUser(user.id, dateBoundary)
    reviewedCommitsCache.addNewUserEntry(newUserCacheEntry)
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