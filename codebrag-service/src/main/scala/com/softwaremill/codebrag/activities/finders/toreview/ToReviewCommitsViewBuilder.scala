package com.softwaremill.codebrag.activities.finders.toreview

import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.activities.finders.AuthorDataAppender
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.finders.views.{CommitState, CommitListView}
import com.softwaremill.codebrag.domain.PartialCommitInfo

class ToReviewCommitsViewBuilder(val userDao: UserDAO, val commitsInfoDao: CommitInfoDAO) extends AuthorDataAppender {

   def toPageView(repoName: String, allBranchCommitsToReview: List[String], paging: PagingCriteria[String]) = {
     val pageOfCommits = paging.extractPageFrom(allBranchCommitsToReview)
     val commits = commitsInfoDao.findByShaList(repoName, pageOfCommits.items)
     val asToReview = markAsToReview(commits)
     addAuthorData(CommitListView(asToReview, pageOfCommits.beforeCount, pageOfCommits.afterCount))
   }

   private def markAsToReview(commits: List[PartialCommitInfo]) = {
     import com.softwaremill.codebrag.activities.finders.CommitToViewImplicits._
     partialCommitListToCommitViewList(commits).map(_.copy(state = CommitState.AwaitingUserReview))
   }

 }
