package com.softwaremill.codebrag.activities.finders.all

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.finders.views.CommitListView
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.cache.BranchCommitsCache
import com.softwaremill.codebrag.activities.finders.UserAndBranch


class AllCommitsFinder(
  protected val repoCache: BranchCommitsCache,
  commitsInfoDao: CommitInfoDAO,
  protected val userDao: UserDAO,
  allCommitsViewBuilder: AllCommitsViewBuilder) extends Logging with UserAndBranch {

  def find(userId: ObjectId, branchName: Option[String], pagingCriteria: PagingCriteria[String]): CommitListView = {
    val user = loadUser(userId)
    val ultimateBranchName = determineBranch(user, branchName)
    val allBranchCommits = repoCache.getBranchCommits(ultimateBranchName).map(_.sha).reverse
    allCommitsViewBuilder.toView(allBranchCommits, pagingCriteria, user)
  }

  def findSingle(sha: String, userId: ObjectId) = {
    commitsInfoDao.findBySha(sha) match {
      case Some(commit) => Right(allCommitsViewBuilder.toViewSingle(commit, userId))
      case None => Left("Commit not found")
    }
  }

}

