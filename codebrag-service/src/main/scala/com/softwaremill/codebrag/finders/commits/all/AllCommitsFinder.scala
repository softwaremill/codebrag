package com.softwaremill.codebrag.finders.commits.all

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.finders.views.CommitListView
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.cache.{RepositoriesCache, RepositoryCache}
import com.softwaremill.codebrag.finders.commits.UserBranchAndRepoPreferences
import com.softwaremill.codebrag.domain.UserBrowsingContext


class AllCommitsFinder(
  protected val repoCache: RepositoriesCache,
  commitsInfoDao: CommitInfoDAO,
  protected val userDao: UserDAO,
  allCommitsViewBuilder: AllCommitsViewBuilder) extends Logging with UserBranchAndRepoPreferences {

  def find(browsingContext: UserBrowsingContext, pagingCriteria: PagingCriteria[String]): CommitListView = {
    val user = loadUser(browsingContext.userId)
    val allBranchCommits = repoCache.getBranchCommits(browsingContext.repoName, browsingContext.branchName).map(_.sha).reverse
    allCommitsViewBuilder.toView(browsingContext.repoName, allBranchCommits, pagingCriteria, user)  // TODO: rething it, passing repo here sux!
  }

  def findSingle(repoName: String, sha: String, userId: ObjectId) = {
    commitsInfoDao.findBySha(repoName, sha) match {
      case Some(commit) => Right(allCommitsViewBuilder.toViewSingle(commit, userId))
      case None => Left("Commit not found")
    }
  }

}

