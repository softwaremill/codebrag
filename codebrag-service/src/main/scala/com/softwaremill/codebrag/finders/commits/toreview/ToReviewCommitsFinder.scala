package com.softwaremill.codebrag.finders.commits.toreview

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.cache.RepositoriesCache
import com.softwaremill.codebrag.dao.finders.views.CommitListView
import com.softwaremill.codebrag.finders.commits.UserBranchAndRepoPreferences
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContextFinder
import com.softwaremill.codebrag.domain.UserBrowsingContext

class ToReviewCommitsFinder(
  protected val repoCache: RepositoriesCache,
  protected val userDao: UserDAO,
  toReviewCommitsFilter: ToReviewBranchCommitsFilter,
  toReviewCommitsViewBuilder: ToReviewCommitsViewBuilder) extends Logging with UserBranchAndRepoPreferences {

  def find(browsingContext: UserBrowsingContext, pagingCriteria: PagingCriteria[String]): CommitListView = {
    val user = loadUser(browsingContext.userId)
    val allBranchCommits = repoCache.getBranchCommits(browsingContext.repoName, browsingContext.branchName)
    val toReviewBranchCommits = toReviewCommitsFilter.filterCommitsToReview(allBranchCommits, user)
    toReviewCommitsViewBuilder.toPageView(browsingContext.repoName, toReviewBranchCommits, pagingCriteria)
  }

  def count(userId: ObjectId, repoName: Option[String], branchName: Option[String]): Long = {
    val user = loadUser(userId)
    val (ultimateRepo, ultimateBranch) = findTargetRepoAndBranchNames(user, repoName, branchName)
    val allBranchCommits = repoCache.getBranchCommits(ultimateRepo, ultimateBranch)
    toReviewCommitsFilter.filterCommitsToReview(allBranchCommits, user).length
  }

  def countForUserRepoAndBranch(userId: ObjectId): Long = count(userId, None, None)

}