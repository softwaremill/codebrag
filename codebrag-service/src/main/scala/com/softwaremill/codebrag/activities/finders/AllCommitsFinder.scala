package com.softwaremill.codebrag.activities.finders

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.finders.views.CommitListView
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.PartialCommitInfo
import CommitToViewImplicits._
import com.softwaremill.codebrag.cache.{UserReviewedCommitsCache, BranchCommitsCache}

class AllCommitsFinder(
  repoCache: BranchCommitsCache,
  val reviewedCommitsCache: UserReviewedCommitsCache,
  commitsInfoDao: CommitInfoDAO,
  val userDao: UserDAO) extends Logging with UserDataEnhancer with CommitReviewedByUserMarker with FullBranchNameResolver {

  def find(userId: ObjectId, branchName: String, pagingCriteria: PagingCriteria[String]): CommitListView = {
    val branchCommits = repoCache.getBranchCommits(resolveFullBranchName(branchName)).map(_.sha).reverse
    val page = pagingCriteria.extractPageFrom(branchCommits)
    val commits = commitsInfoDao.findByShaList(page.items)
    enhanceWithUserData(CommitListView(markAsReviewed(commits, userId), page.beforeCount, page.afterCount))
  }

  def findSingle(sha: String, userId: ObjectId) = {
    commitsInfoDao.findBySha(sha) match {
      case Some(commit) => Right(markAsReviewed(enhanceWithUserData(PartialCommitInfo(commit)), userId))
      case None => Left("Commit not found")
    }
  }

}