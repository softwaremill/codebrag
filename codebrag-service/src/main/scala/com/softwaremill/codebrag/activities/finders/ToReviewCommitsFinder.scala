package com.softwaremill.codebrag.activities.finders

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.domain.{PartialCommitInfo, User}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging
import CommitToViewImplicits._
import com.softwaremill.codebrag.cache.{UserReviewedCommitsCache, BranchCommitsCache, BranchCommitCacheEntry}
import com.softwaremill.codebrag.service.config.ReviewProcessConfig
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.CommitAuthorClassification._
import com.softwaremill.codebrag.dao.finders.views.{CommitState, CommitListView}

class ToReviewCommitsFinder(
  config: ReviewProcessConfig,
  repoCache: BranchCommitsCache,
  reviewedCommitsCache: UserReviewedCommitsCache,
  commitsInfoDao: CommitInfoDAO,
  val userDao: UserDAO) extends Logging with AuthorDataAppender {

  def find(userId: ObjectId, branchName: String, pagingCriteria: PagingCriteria[String]): CommitListView = {
    val toReview = getSHAsOfCommitsToReview(userId, Option(branchName))
    val page = pagingCriteria.extractPageFrom(toReview)
    val commits = commitsInfoDao.findByShaList(page.items)
    val asToReview = markAsToReview(commits)
    addAuthorData(CommitListView(asToReview, page.beforeCount, page.afterCount))
  }

  def count(userId: ObjectId, branchName: String): Long = {
    getSHAsOfCommitsToReview(userId, Option(branchName)).size
  }

  def countForUserSelectedBranch(userId: ObjectId): Long = {
    getSHAsOfCommitsToReview(userId, None).size
  }

  private def markAsToReview(commits: List[PartialCommitInfo]) = {
    partialCommitListToCommitViewList(commits).map(_.copy(state = CommitState.AwaitingUserReview))
  }

  private def getSHAsOfCommitsToReview(userId: ObjectId, branchName: Option[String]): List[String] = {
    userDao.findById(userId).map { user =>
      val userSelectedBranch = user.settings.selectedBranch.getOrElse(repoCache.repository.getCheckedOutBranchFullName)
      findShaToReview(userSelectedBranch, user)
    }.getOrElse(List.empty)
  }

  private def findShaToReview(branchName: String, user: User): List[String] = {
    val userBoundaryDate = reviewedCommitsCache.getUserEntry(user.id).toReviewStartDate
    val commitsInBranch = repoCache.getBranchCommits(branchName)
    commitsInBranch
      .filterNot(userOrDoneCommits(_, user))
      .takeWhile(commitsAfterUserDate(_, userBoundaryDate))
      .filter(notYetFullyReviewed)
      .map(_.sha)
      .reverse
  }

  private def userOrDoneCommits(commitEntry: BranchCommitCacheEntry, user: User) = {
    commitAuthoredByUser(commitEntry, user) || userAlreadyReviewed(user.id, commitEntry)
  }


  private def commitsAfterUserDate(commitEntry: BranchCommitCacheEntry, userBoundaryDate: DateTime): Boolean = {
    commitEntry.commitDate.isAfter(userBoundaryDate) || commitEntry.commitDate.isEqual(userBoundaryDate)
  }

  private def notYetFullyReviewed(commitEntry: BranchCommitCacheEntry): Boolean = {
    reviewedCommitsCache.usersWhoReviewed(commitEntry.sha).size < config.requiredReviewersCount
  }

  private def userAlreadyReviewed(userId: ObjectId, commit: BranchCommitCacheEntry): Boolean = {
    val commitsReviewedByUser = reviewedCommitsCache.getUserEntry(userId).commits
    commitsReviewedByUser.find(_.sha == commit.sha).nonEmpty
  }

}