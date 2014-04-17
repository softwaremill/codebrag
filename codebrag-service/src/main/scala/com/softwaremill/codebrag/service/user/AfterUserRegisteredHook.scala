package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.domain._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import CommitAuthorClassification._
import org.joda.time.DateTime
import com.softwaremill.codebrag.cache.{UserReviewedCommitsCacheEntry, UserReviewedCommitsCache, BranchCommitsCache}
import com.softwaremill.codebrag.service.config.ReviewProcessConfig

class AfterUserRegisteredHook(
  val repoCache: BranchCommitsCache,
  val reviewedCommitsCache: UserReviewedCommitsCache,
  val config: ReviewProcessConfig) extends SetStartingDateForUser {

  def run(user: NewUserRegistered) {
    setStartingDate(user)
  }

}

trait SetStartingDateForUser extends Logging {

  val repoCache: BranchCommitsCache
  val reviewedCommitsCache: UserReviewedCommitsCache
  val config: ReviewProcessConfig

  protected def setStartingDate(user: NewUserRegistered) {
    val defaultToReviewStartDate = user.timestamp.minusDays(config.initialToReviewDays)
    val allBranchCommits = repoCache.getBranchCommits(currentBranch)
    val newestForUserToReview = allBranchCommits.filterNot(commitAuthoredByUser(_, user)).take(config.initialCommitsToReviewCount)
    val dateBoundary = newestForUserToReview.lastOption match {
      case Some(last) => getLatestFromBothDates(last.commitDate, defaultToReviewStartDate)
      case None => defaultToReviewStartDate
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

  private def currentBranch = repoCache.repository.getCheckedOutBranchFullName

}