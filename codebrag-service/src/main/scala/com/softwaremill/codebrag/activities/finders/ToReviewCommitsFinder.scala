package com.softwaremill.codebrag.activities.finders

import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.commits.branches.{ReviewedCommitsCache, RepositoryCache}
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.domain.{CommitAuthorClassification, PartialCommitInfo}
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
    userDAO.findById(userId).map { user =>
      val alreadyReviewedCommits = reviewedCommitsCache.reviewedByUser(userId)
      logger.debug(s"User has ${alreadyReviewedCommits.size} commits already reviewed")
      val branchCommits = repoCache.getBranchCommits(branchName)
      import CommitAuthorClassification._
      val toReview = branchCommits.filterNot(commit => alreadyReviewedCommits.contains(commit) || commitAuthoredByUser(commit, user)).map(_.sha)
      logger.debug(s"User has ${toReview.size} commits left to review for branch ${branchName}")
      toReview.reverse // get commits in correct order to display (older first)
    } getOrElse(List.empty)
  }

  implicit def partialCommitListToCommitViewList(commits: List[PartialCommitInfo]): List[CommitView] = {
    commits.map(partialCommitToCommitView)
  }

  implicit def partialCommitToCommitView(commit: PartialCommitInfo): CommitView = {
    CommitView(commit.id.toString, commit.sha, commit.message, commit.authorName, commit.authorEmail, commit.date.toDate)
  }
}