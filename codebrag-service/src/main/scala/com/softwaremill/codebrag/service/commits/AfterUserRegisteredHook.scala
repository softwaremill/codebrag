package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.domain._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.service.commits.branches.{ReviewedCommitsCache, RepositoryCache}
import CommitAuthorClassification._

class AfterUserRegisteredHook(val repoCache: RepositoryCache, val reviewedCommitsCache: ReviewedCommitsCache) extends PrepareCommitsToReview {

  def run(user: NewUserRegistered) {
    prepareCommitsToReviewFor(user)
  }

}

trait PrepareCommitsToReview extends Logging {

  val repoCache: RepositoryCache
  val reviewedCommitsCache: ReviewedCommitsCache

  private val LastCommitsToReviewCount = 10

  protected def prepareCommitsToReviewFor(user: NewUserRegistered) {
    logger.debug(s"New user ${user.login} registered. Preparing reviewed/to review commits")
    val commitsToLeaveForReview = repoCache.getBranchNames.map { branch =>
      val branchCommits = repoCache.getBranchCommits(branch)
        .filterNot(commitAuthoredByUser(_, user))
        .take(LastCommitsToReviewCount)
      logger.debug(s"Commits left to review for branch ${branch} (${branchCommits.size}): ${branchCommits.map(_.sha.substring(0, 5))}")
      branchCommits
    }.flatten.toSet
    logger.debug(s"${commitsToLeaveForReview.size} commit(s) left for review for new user ${user.login}")
    val toMarkAsReviewed = repoCache.getAllCommits.filterNot(commitsToLeaveForReview.contains)
    logger.debug(s"${toMarkAsReviewed.size} commit(s) to mark as reviewed for new user ${user.login}")
    reviewedCommitsCache.markCommitsAsReviewedBy(user.userId, toMarkAsReviewed)
  }

}