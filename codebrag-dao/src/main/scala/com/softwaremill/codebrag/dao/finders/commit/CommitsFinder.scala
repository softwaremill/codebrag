package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.dao.commitinfo.{CommitInfoDAO}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.commit.OutOfPageCommitCounter._
import com.softwaremill.codebrag.dao.finders.views.{CommitView, CommitListView}
import com.softwaremill.codebrag.domain.PartialCommitInfo
import com.softwaremill.codebrag.common.paging.PagingCriteria

trait CommitsFinder extends UserDataEnhancer {
  def commitInfoDAO: CommitInfoDAO

  def findCommits(
    ids: List[ObjectId],
    paging: PagingCriteria[ObjectId],
    transformCommits: List[CommitView] => List[CommitView]) = {

    val pageOfCommits = commitInfoDAO.findPartialCommitInfo(paging.extractPageFrom(ids))
    val commits = toCommitViews(pageOfCommits)
    val numOlder = countOlderCommits(ids.map(_.toString), commits)
    val numNewer = countNewerCommits(ids.map(_.toString), commits)
    enhanceWithUserData(CommitListView(transformCommits(commits), numOlder, numNewer))
  }

  private def toCommitViews(commits: List[PartialCommitInfo]) = {
    commits.map(toCommitView)
  }

  protected def toCommitView(commit: PartialCommitInfo) = {
    CommitView(commit.id.toString, commit.sha, commit.message, commit.authorName, commit.authorEmail, commit.date.toDate)
  }
}
