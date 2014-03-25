package com.softwaremill.codebrag.activities.finders

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.domain.{User, CommitAuthorClassification}
import com.softwaremill.codebrag.dao.finders.views.CommitListView
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging
import CommitToViewImplicits._
import com.softwaremill.codebrag.cache.{UserReviewedCommitsCache, BranchCommitsCache, BranchCommitCacheEntry}

class ToReviewCommitsFinder(
  repoCache: BranchCommitsCache,
  reviewedCommitsCache: UserReviewedCommitsCache,
  commitsInfoDao: CommitInfoDAO,
  val userDao: UserDAO) extends Logging with UserDataEnhancer with FullBranchNameResolver {

  def find(userId: ObjectId, branchName: String, pagingCriteria: PagingCriteria[String]): CommitListView = {
    val toReview = getSHAsOfCommitsToReview(userId, resolveFullBranchName(branchName))
    val page = pagingCriteria.extractPageFrom(toReview)
    val commits = commitsInfoDao.findByShaList(page.items)
    enhanceWithUserData(CommitListView(commits, page.beforeCount, page.afterCount))
  }

  def count(userId: ObjectId, branchName: String): Long = {
    getSHAsOfCommitsToReview(userId, resolveFullBranchName(branchName)).size
  }

  private def getSHAsOfCommitsToReview(userId: ObjectId, fullBranchName: String): List[String] = {
    userDao.findById(userId).map(findShaToReview(fullBranchName, _)).getOrElse(List.empty)
  }

  private def findShaToReview(fullBranchName: String, user: User): List[String] = {
    import CommitAuthorClassification._
    val userBoundaryDate = reviewedCommitsCache.getUserEntry(user.id).toReviewStartDate
    val commitsInBranch = repoCache.getBranchCommits(fullBranchName)
    val toReview = commitsInBranch
      .filterNot(commit => commitAuthoredByUser(commit, user) || userAlreadyReviewed(user.id, commit))
      // TODO: add step to filter out commits that were already reviewed by number of users
      .takeWhile( c => c.commitDate.isAfter(userBoundaryDate) || c.commitDate.isEqual(userBoundaryDate))
    toReview.reverse.map(_.sha)
  }


  private def userAlreadyReviewed(userId: ObjectId, commit: BranchCommitCacheEntry): Boolean = {
    val commitsReviewedByUser = reviewedCommitsCache.getUserEntry(userId).commits
    commitsReviewedByUser.find(_.sha == commit.sha).nonEmpty
  }

}