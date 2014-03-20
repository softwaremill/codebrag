package com.softwaremill.codebrag.activities.finders

import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.commits.branches.{CommitCacheEntry, UserReviewedCommitsCache, RepositoryCache}
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.domain.{User, CommitAuthorClassification}
import com.softwaremill.codebrag.dao.finders.views.CommitListView
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging
import CommitToViewImplicits._

class ToReviewCommitsFinder(
  repoCache: RepositoryCache,
  reviewedCommitsCache: UserReviewedCommitsCache,
  commitsInfoDao: CommitInfoDAO,
  val userDAO: UserDAO) extends Logging with UserDataEnhancer {

  def find(userId: ObjectId, branchName: String, pagingCriteria: PagingCriteria[String]): CommitListView = {
    val toReview = getSHAsOfCommitsToReview(userId, branchName)
    val page = pagingCriteria.extractPageFrom(toReview)
    val commits = commitsInfoDao.findByShaList(page.items)
    enhanceWithUserData(CommitListView(commits, page.beforeCount, page.afterCount))
  }

  private def getSHAsOfCommitsToReview(userId: ObjectId, branchName: String): List[String] = {
    userDAO.findById(userId).map(findShaToReview(branchName, _)).getOrElse(List.empty)
  }


  def findShaToReview(branchName: String, user: User): List[String] = {
    import CommitAuthorClassification._
    val userBoundaryDate = reviewedCommitsCache.getUserEntry(user.id).toReviewStartDate
    val commitsInBranch = repoCache.getBranchCommits(branchName)
    val toReview = commitsInBranch
      .filterNot(commit => commitAuthoredByUser(commit, user) || userAlreadyReviewed(user.id, commit))
      // TODO: add step to filter out commits that were already reviewed by number of users
      .takeWhile( c => c.commitDate.isAfter(userBoundaryDate) || c.commitDate.isEqual(userBoundaryDate))
    toReview.reverse.map(_.sha)
  }


  private def userAlreadyReviewed(userId: ObjectId, commit: CommitCacheEntry): Boolean = {
    val commitsReviewedByUser = reviewedCommitsCache.getUserEntry(userId).commits
    commitsReviewedByUser.find(_.sha == commit.sha).nonEmpty
  }

}