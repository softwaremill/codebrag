package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.dao.commitinfo.{CommitInfoDAO}
import com.softwaremill.codebrag.common.LoadMoreCriteria
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.finders.commit.ListSliceLoader._
import com.softwaremill.codebrag.dao.finders.commit.OutOfPageCommitCounter._
import com.softwaremill.codebrag.dao.finders.views.{CommitView, CommitListView}
import com.softwaremill.codebrag.domain.PartialCommitInfo

trait CommitsFinder extends UserDataEnhancer {
  def commitInfoDAO: CommitInfoDAO

  def findCommits(
    ids: List[ObjectId],
    paging: LoadMoreCriteria,
    transformCommits: List[CommitView] => List[CommitView]) = {

    val commitsSlice = loadSliceUsing(paging, ids, commitInfoDAO.findPartialCommitInfo)
    val commits = toCommitViews(commitsSlice)
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
