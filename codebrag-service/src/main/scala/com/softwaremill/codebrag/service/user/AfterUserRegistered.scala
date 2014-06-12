package com.softwaremill.codebrag.service.user

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import org.joda.time.DateTime
import com.softwaremill.codebrag.cache.{UserReviewedCommitsCacheEntry, UserReviewedCommitsCache}
import com.softwaremill.codebrag.service.config.ReviewProcessConfig

class AfterUserRegistered(val reviewedCommitsCache: UserReviewedCommitsCache, val config: ReviewProcessConfig) extends SetStartingDateForUser {

  def run(user: NewUserRegistered) {
    setStartingDate(user)
  }

}

trait SetStartingDateForUser extends Logging {

  protected val reviewedCommitsCache: UserReviewedCommitsCache
  protected val config: ReviewProcessConfig

  protected def setStartingDate(user: NewUserRegistered) {
    val toReviewStartDate = user.timestamp.minusDays(config.initialToReviewDays)
    val newUserCacheEntry = UserReviewedCommitsCacheEntry.forNewlyRegisteredUser(user.id, toReviewStartDate)
    reviewedCommitsCache.addNewUserEntry(newUserCacheEntry)
    logger.debug(s"New user ${user.login} registered. To review boundary date set to $toReviewStartDate")
  }

  def getLatestFromBothDates(lastCommitDate: DateTime, weekBeforeRegistration: DateTime): DateTime = {
    if (lastCommitDate.isBefore(weekBeforeRegistration)) {
      weekBeforeRegistration
    } else {
      lastCommitDate
    }
  }

}