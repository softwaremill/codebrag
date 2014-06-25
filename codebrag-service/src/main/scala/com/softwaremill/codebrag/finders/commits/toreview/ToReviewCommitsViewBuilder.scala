package com.softwaremill.codebrag.finders.commits.toreview

import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.finders.views.{CommitView, CommitState, CommitListView}
import com.softwaremill.codebrag.domain.{CommitInfo, PartialCommitInfo}
import com.softwaremill.codebrag.finders.commits.AuthorDataAppender

class ToReviewCommitsViewBuilder(val userDao: UserDAO, val commitsInfoDao: CommitInfoDAO) extends AuthorDataAppender {

   def toPageView(repoName: String, allBranchCommitsToReview: List[String], paging: PagingCriteria[String]) = {
     val pageOfCommits = paging.extractPageFrom(allBranchCommitsToReview)
     val commits = commitsInfoDao.findByShaList(repoName, pageOfCommits.items)
     val asToReview = markAsToReview(repoName, commits)
     addAuthorData(CommitListView(asToReview, pageOfCommits.beforeCount, pageOfCommits.afterCount))
   }

   private def markAsToReview(repoName: String, commits: List[PartialCommitInfo]) = {
     toCommitsListView(repoName, commits).map(_.copy(state = CommitState.AwaitingUserReview))
   }

  private def toCommitsListView(repoName:String, commits: List[PartialCommitInfo]) = {
    commits.map { commit =>
      CommitView(commit.id.toString, repoName, commit.sha, commit.message, commit.authorName, commit.authorEmail, commit.date.toDate)
    }
  }

}
