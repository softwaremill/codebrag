package com.softwaremill.codebrag.finders.commits.toreview

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.user.{UserDAO, TeamDAO}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.cache.RepositoriesCache
import com.softwaremill.codebrag.dao.finders.views.CommitListView
import com.softwaremill.codebrag.finders.commits.{UserLoader, TeamMemberLoader}
import com.softwaremill.codebrag.finders.browsingcontext.{UserBrowsingContext, UserBrowsingContextFinder}
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.Team

class ToReviewCommitsFinder(
  protected val repoCache: RepositoriesCache,
  protected val userDao: UserDAO,
  protected val teamDao: TeamDAO,
  userBrowsingContextFinder: UserBrowsingContextFinder,
  toReviewCommitsFilter: ToReviewBranchCommitsFilter,
  toReviewCommitsViewBuilder: ToReviewCommitsViewBuilder) extends Logging with UserLoader with TeamMemberLoader {

  def find(browsingContext: UserBrowsingContext, pagingCriteria: PagingCriteria[String]): CommitListView = {
    val user = loadUser(browsingContext.userId)
    val teamMembers = loadTeamMembersWithDetails(browsingContext.userId)
    val allBranchCommits = repoCache.getBranchCommits(browsingContext.repoName, browsingContext.branchName)
    val toReviewBranchCommits = toReviewCommitsFilter.filterCommitsToReview(allBranchCommits, user, teamMembers, browsingContext.repoName)
    toReviewCommitsViewBuilder.toPageView(browsingContext.repoName, toReviewBranchCommits, pagingCriteria)
  }

  def count(browsingContext: UserBrowsingContext): Long = {
    val user = loadUser(browsingContext.userId)
    val teamMembers = loadTeamMembersWithDetails(browsingContext.userId)
    val allBranchCommits = repoCache.getBranchCommits(browsingContext.repoName, browsingContext.branchName)
    toReviewCommitsFilter.filterCommitsToReview(allBranchCommits, user, teamMembers, browsingContext.repoName).length
  }

  def countSince(date: DateTime, browsingContext: UserBrowsingContext): Long = {
    val user = loadUser(browsingContext.userId)
    val teamMembers = loadTeamMembersWithDetails(browsingContext.userId)
    val branchCommits = repoCache
      .getBranchCommits(browsingContext.repoName, browsingContext.branchName)
      .filter(bc => bc.commitDate.isAfter(date) || bc.commitDate.isEqual(date))
    toReviewCommitsFilter.filterCommitsToReview(branchCommits, user, teamMembers, browsingContext.repoName).length
  }

  def countForUserRepoAndBranch(userId: ObjectId): Long = {
    val userDefaultContext = userBrowsingContextFinder.findUserDefaultContext(userId)
    count(userDefaultContext)
  }
}