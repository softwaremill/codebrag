package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.domain._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import CommitAuthorClassification._
import org.joda.time.DateTime
import com.softwaremill.codebrag.cache.{RepositoriesCache, RepositoryCache}
import com.softwaremill.codebrag.service.config.ReviewProcessConfig
import com.softwaremill.codebrag.dao.repo.UserRepoDetailsDAO

class AfterUserRegistered(
  repoCache: RepositoriesCache,
  userRepoDetails: UserRepoDetailsDAO,
  config: ReviewProcessConfig) {

  def run(user: NewUserRegistered) {
    import ToReviewStartDateCalculator.calcStartingDate
    val contexts = repoCache.repoNames.map { repoName =>
      val startingDate = calcStartingDate(user, repoCache.getRepo(repoName), config)
      val defaultBranch = repoCache.getCheckedOutBranchShortName(repoName)
      UserRepoDetails(user.id, repoName, defaultBranch, startingDate)
    }
    contexts.foreach(userRepoDetails.save)
  }

}

object ToReviewStartDateCalculator extends Logging {

  def calcStartingDate(registeredUser: NewUserRegistered, repoCache: RepositoryCache, config: ReviewProcessConfig) = {
    val defaultToReviewStartDate = registeredUser.timestamp.minusDays(config.initialToReviewDays)
    val repoDefaultBranch = repoCache.repository.getCheckedOutBranchFullName
    val allBranchCommits = repoCache.getBranchCommits(repoDefaultBranch)
    val newestForUserToReview = allBranchCommits.filterNot(commitAuthoredByUser(_, registeredUser)).take(config.initialCommitsToReviewCount)
    newestForUserToReview.lastOption match {
      case Some(last) => getLatestFromBothDates(last.commitDate, defaultToReviewStartDate)
      case None => defaultToReviewStartDate
    }    
  }

  private def getLatestFromBothDates(lastCommitDate: DateTime, dateRangeBeforeRegistration: DateTime): DateTime = {
    if (lastCommitDate.isBefore(dateRangeBeforeRegistration)) dateRangeBeforeRegistration else lastCommitDate
  }

}