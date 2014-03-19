package com.softwaremill.codebrag.activities.finders

import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.commits.branches.{CommitCacheEntry, ReviewedCommitsCache, RepositoryCache}
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.domain.{User, CommitAuthorClassification, PartialCommitInfo}
import com.softwaremill.codebrag.dao.finders.views.{CommitListView, CommitView}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging

class ToReviewCommitsFinder(
  repoCache: RepositoryCache,
  reviewedCommitsCache: ReviewedCommitsCache,
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
    val userBoundaryDate = reviewedCommitsCache.getToReviewStartDateForUser(user.id)
    val commitsInBranch = repoCache.getBranchCommits(branchName)
    val toReview = commitsInBranch
      .filterNot(commit => commitAuthoredByUser(commit, user) || userAlreadyReviewed(user.id, commit))
      // TODO: add step to filter out commits that were already reviewed by number of users
      .takeWhile( c => c.commitDate.isAfter(userBoundaryDate) || c.commitDate.isEqual(userBoundaryDate))
    toReview.reverse.map(_.sha)
  }


  private def userAlreadyReviewed(userId: ObjectId, commit: CommitCacheEntry): Boolean = {
    reviewedCommitsCache.reviewedByUser(userId).contains(commit)
  }

  implicit def partialCommitListToCommitViewList(commits: List[PartialCommitInfo]): List[CommitView] = {
    commits.map(partialCommitToCommitView)
  }

  implicit def partialCommitToCommitView(commit: PartialCommitInfo): CommitView = {
    CommitView(commit.id.toString, commit.sha, commit.message, commit.authorName, commit.authorEmail, commit.date.toDate)
  }
}