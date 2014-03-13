package com.softwaremill.codebrag.activities.finders

import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.commits.branches.{ReviewedCommitsCache, RepositoryCache}
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.finders.views.{CommitView, CommitListView}
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging
import java.util.Date

class AllCommitsFinder(
  repoCache: RepositoryCache,
  reviewedCommitsCache: ReviewedCommitsCache,
  commitsInfoDao: CommitInfoDAO,
  val userDAO: UserDAO) extends Logging with UserDataEnhancer {

  def find(userId: ObjectId, branchName: String, pagingCriteria: PagingCriteria[String]): CommitListView = {
    logger.debug("IMPLEMENT ME!")
    CommitListView(List.empty, 0, 0)
  }

  def find(commitId: ObjectId, userId: ObjectId) = {
    logger.debug("IMPLEMENT ME!")
    Right(CommitView("", "", "", "", "", new Date))
  }

}