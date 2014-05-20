package com.softwaremill.codebrag.activities.finders.toreview

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.cache.BranchCommitsCache
import com.softwaremill.codebrag.dao.finders.views.CommitListView
import com.softwaremill.codebrag.activities.finders.UserAndBranch

class ToReviewCommitsFinder(
                             protected val repoCache: BranchCommitsCache,
                             protected val userDao: UserDAO,
                             toReviewCommitsFilter: ToReviewBranchCommitsFilter,
                             toReviewCommitsViewBuilder: ToReviewCommitsViewBuilder) extends Logging with UserAndBranch {

  def find(userId: ObjectId, providedBranchName: Option[String], pagingCriteria: PagingCriteria[String]): CommitListView = {
    val user = loadUser(userId)
    val ultimateBranchName = determineBranch(user, providedBranchName)
    val allBranchCommits = repoCache.getBranchCommits(ultimateBranchName) 
    val toReviewBranchCommits = toReviewCommitsFilter.filterFor(allBranchCommits, user)
    toReviewCommitsViewBuilder.toPageView(toReviewBranchCommits, pagingCriteria)
  }

  def count(userId: ObjectId, branchName: Option[String]): Long = {
    val user = loadUser(userId)
    val ultimateBranchName = determineBranch(user, branchName)
    val allBranchCommits = repoCache.getBranchCommits(ultimateBranchName)
    toReviewCommitsFilter.filterFor(allBranchCommits, user).length
  }

  def countForUserSelectedBranch(userId: ObjectId): Long = count(userId, None)

}