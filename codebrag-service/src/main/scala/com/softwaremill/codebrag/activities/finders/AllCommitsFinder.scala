package com.softwaremill.codebrag.activities.finders

import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.commits.branches.{UserReviewedCommitsCache, BranchCommitsCache}
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.finders.views.CommitListView
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.PartialCommitInfo
import CommitToViewImplicits._

class AllCommitsFinder(
  repoCache: BranchCommitsCache,
  val reviewedCommitsCache: UserReviewedCommitsCache,
  commitsInfoDao: CommitInfoDAO,
  val userDao: UserDAO) extends Logging with UserDataEnhancer with CommitReviewedByUserMarker {

  def find(userId: ObjectId, branchName: String, pagingCriteria: PagingCriteria[String]): CommitListView = {
    val branchCommits = repoCache.getBranchCommits(branchName).map(_.sha).reverse
    val page = pagingCriteria.extractPageFrom(branchCommits)
    val commits = commitsInfoDao.findByShaList(page.items)
    enhanceWithUserData(CommitListView(markAsReviewed(commits, userId), page.beforeCount, page.afterCount))
  }

  // TODO: change to Option
  def find(sha: String, userId: ObjectId) = {
    commitsInfoDao.findBySha(sha) match {
      case Some(commit) => Right(markAsReviewed(enhanceWithUserData(PartialCommitInfo(commit)), userId))
      case None => Left("Commit not found")
    }
  }

}