package com.softwaremill.codebrag.activities.finders.all

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.finders.views.CommitListView
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.cache.{RepositoriesCache, RepositoryCache}
import com.softwaremill.codebrag.activities.finders.UserBranchAndRepoPreferences


class AllCommitsFinder(
  protected val repoCache: RepositoriesCache,
  commitsInfoDao: CommitInfoDAO,
  protected val userDao: UserDAO,
  allCommitsViewBuilder: AllCommitsViewBuilder) extends Logging with UserBranchAndRepoPreferences {

  def find(userId: ObjectId, repoNameOpt: Option[String], branchNameOpt: Option[String], pagingCriteria: PagingCriteria[String]): CommitListView = {
    val user = loadUser(userId)
    val (repoName, branchName) = findTargetRepoAndBranchNames(user, repoNameOpt, branchNameOpt)
    val allBranchCommits = repoCache.getBranchCommits(repoName, branchName).map(_.sha).reverse
    allCommitsViewBuilder.toView(repoName, allBranchCommits, pagingCriteria, user)  // TODO: rething it, passing repo here sux!
  }

  def findSingle(sha: String, userId: ObjectId) = {
    commitsInfoDao.findBySha(sha) match {
      case Some(commit) => Right(allCommitsViewBuilder.toViewSingle(commit, userId))
      case None => Left("Commit not found")
    }
  }

}

