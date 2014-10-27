package com.softwaremill.codebrag.finders.commits.all

import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.service.config.ReviewProcessConfig
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.cache.UserReviewedCommitsCache
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.domain.{PartialCommitInfo, CommitInfo, User}
import com.softwaremill.codebrag.dao.finders.views.{CommitView, CommitListView}
import com.softwaremill.codebrag.finders.commits.{ReviewersDataAppender, CommitReviewStateAppender, AuthorDataAppender}
import org.bson.types.ObjectId

class AllCommitsViewBuilder(
  val commitsInfoDao: CommitInfoDAO,
  val config: ReviewProcessConfig,
  val userDao: UserDAO,
  val reviewedCommitsCache: UserReviewedCommitsCache) extends Logging with AuthorDataAppender with CommitReviewStateAppender with ReviewersDataAppender {

  def toView(repoName: String, allBranchCommits: List[String], pagingCriteria: PagingCriteria[String], user: User) = {
    val page = pagingCriteria.extractPageFrom(allBranchCommits)
    val commits = commitsInfoDao.findByShaList(repoName, page.items)
    addAuthorData(CommitListView(setCommitsReviewStates(toCommitsListView(repoName, commits), user.id), page.beforeCount, page.afterCount))
  }

  def toViewSingle(commit: CommitInfo, userId: ObjectId) = {
    Option(commit)
      .map(toCommitView)
      .map(addAutorData)
      .map(addReviewersData)
      .map(setCommitReviewState(_, userId))
      .get
  }

  private def toCommitsListView(repoName:String, commits: List[PartialCommitInfo]) = {
    commits.map { commit =>
        CommitView(commit.id.toString, repoName, commit.sha, commit.message, commit.authorName, commit.authorEmail, commit.date)
    }
  }
  
  private def toCommitView(commit: CommitInfo) = {
    CommitView(commit.id.toString, commit.repoName, commit.sha, commit.message, commit.authorName, commit.authorEmail, commit.commitDate)
  }

}
