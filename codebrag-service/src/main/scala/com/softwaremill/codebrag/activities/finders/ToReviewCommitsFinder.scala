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
  val config: ReviewProcessConfig,
  repoCache: BranchCommitsCache,
  val reviewedCommitsCache: UserReviewedCommitsCache,
  val commitsInfoDao: CommitInfoDAO,
  val userDao: UserDAO) extends Logging with FindToReviewCommitsInBranch with BuildToReviewCommitsView {

  def find(userId: ObjectId, branchName: Option[String], pagingCriteria: PagingCriteria[String]): CommitListView = {
    val toReview = allCommitsToReviewFor(userId, branchName)
    buildToReviewCommitsView(toReview, pagingCriteria)
  }

  def count(userId: ObjectId, branchName: Option[String]): Long = {
    allCommitsToReviewFor(userId, branchName).size
  }

  def countForUserSelectedBranch(userId: ObjectId): Long = {
    allCommitsToReviewFor(userId, None).size
  }

  private def allCommitsToReviewFor(userId: ObjectId, branchName: Option[String]): List[String] = {
    userDao.findById(userId).map { user =>
      val ultimateBranchName = determineBranch(branchName, user.settings.selectedBranch)
      findToReviewCommitsInBranch(getBranchCommits(ultimateBranchName), user)
    }.getOrElse(List.empty)
  }

  private def determineBranch(provided: Option[String], userSelected: Option[String]) = {
    provided.getOrElse(userSelected.getOrElse(repoCache.getCheckedOutBranchShortName))
  }

  private def getBranchCommits(branchName: String) = repoCache.getBranchCommits(branchName)


}

protected[finders] trait FindToReviewCommitsInBranch {

  protected def config: ReviewProcessConfig
  protected def reviewedCommitsCache: UserReviewedCommitsCache

  protected[finders] def findToReviewCommitsInBranch(branchCommits: List[BranchCommitCacheEntry], user: User): List[String] = {
    val userBoundaryDate = reviewedCommitsCache.getUserEntry(user.id).toReviewStartDate
    branchCommits
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

protected[finders] trait BuildToReviewCommitsView extends AuthorDataAppender {

  protected def commitsInfoDao: CommitInfoDAO

  protected[finders] def buildToReviewCommitsView(allBranchCommitsToReview: List[String], paging: PagingCriteria[String]) = {
    val pageOfCommits = paging.extractPageFrom(allBranchCommitsToReview)
    val commits = commitsInfoDao.findByShaList(pageOfCommits.items)
    val asToReview = markAsToReview(commits)
    addAuthorData(CommitListView(asToReview, pageOfCommits.beforeCount, pageOfCommits.afterCount))    
  }

  private def markAsToReview(commits: List[PartialCommitInfo]) = {
    partialCommitListToCommitViewList(commits).map(_.copy(state = CommitState.AwaitingUserReview))
  }

}