package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.views.{CommitView, CommitListView}
import com.softwaremill.codebrag.domain.PartialCommitInfo
import com.softwaremill.codebrag.common.paging.PagingCriteria

trait CommitsFinder extends UserDataEnhancer {
  def commitInfoDAO: CommitInfoDAO

  def findCommits(
    ids: List[ObjectId],
    paging: PagingCriteria[ObjectId],
    transformCommits: List[CommitView] => List[CommitView]) = {

    val page = paging.extractPageFrom(ids)
    val commits = commitInfoDAO.findPartialCommitInfo(page.items).map(toCommitView)
    enhanceWithUserData(CommitListView(transformCommits(commits), page.beforeCount, page.afterCount))
  }

  protected def toCommitView(commit: PartialCommitInfo) = {
    CommitView(commit.id.toString, commit.sha, commit.message, commit.authorName, commit.authorEmail, commit.date.toDate)
  }
}