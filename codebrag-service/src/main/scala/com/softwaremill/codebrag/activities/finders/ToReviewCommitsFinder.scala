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
    val toReview = getSHAsOfCommitsToReview(userId, branchName)
    val page = pagingCriteria.extractPageFrom(toReview)
    val commits = commitsInfoDao.findByShaList(page.items)
    val asToReview = markAsToReview(commits)
    addAuthorData(CommitListView(asToReview, page.beforeCount, page.afterCount))
  }

  def count(userId: ObjectId, branchName: String): Long = {
    getSHAsOfCommitsToReview(userId, branchName).size
  }

  def countForCurrentBranch(userId: ObjectId): Long = {
    val fullBranchName = repoCache.repository.getCheckedOutBranchFullName
    count(userId, fullBranchName)
  }

  private def markAsToReview(commits: List[PartialCommitInfo]) = {
    partialCommitListToCommitViewList(commits).map(_.copy(state = CommitState.AwaitingUserReview))
  }

  private def getSHAsOfCommitsToReview(userId: ObjectId, fullBranchName: String): List[String] = {
    userDao.findById(userId).map(findShaToReview(fullBranchName, _)).getOrElse(List.empty)
  }

  private def findShaToReview(fullBranchName: String, user: User): List[String] = {
    val userBoundaryDate = reviewedCommitsCache.getUserEntry(user.id).toReviewStartDate
    val commitsInBranch = repoCache.getBranchCommits(fullBranchName)
    commitsInBranch
      .filterNot(userOrDoneCommits(_, user))
      .takeWhile(commitsAfterUserDate(_, userBoundaryDate))
      .filter(notYetFullyReviewed)
      .map(_.sha)
      .reverse
  }
  
  def userOrDoneCommits(commitEntry: BranchCommitCacheEntry, user: User) = {
    commitAuthoredByUser(commitEntry, user) || userAlreadyReviewed(user.id, commitEntry)
  }


  def commitsAfterUserDate(commitEntry: BranchCommitCacheEntry, userBoundaryDate: DateTime): Boolean = {
    commitEntry.commitDate.isAfter(userBoundaryDate) || commitEntry.commitDate.isEqual(userBoundaryDate)
  }

  def notYetFullyReviewed(commitEntry: BranchCommitCacheEntry): Boolean = {
    reviewedCommitsCache.usersWhoReviewed(commitEntry.sha).size < config.requiredReviewersCount
  }

  private def userAlreadyReviewed(userId: ObjectId, commit: BranchCommitCacheEntry): Boolean = {
    val commitsReviewedByUser = reviewedCommitsCache.getUserEntry(userId).commits
    commitsReviewedByUser.find(_.sha == commit.sha).nonEmpty
  }

}