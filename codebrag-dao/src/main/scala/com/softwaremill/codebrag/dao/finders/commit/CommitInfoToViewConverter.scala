package com.softwaremill.codebrag.dao.finders.commit

import org.bson.types.ObjectId
import java.util.Date
import com.softwaremill.codebrag.dao.reporting.views.CommitView
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.dao.commitinfo.{PartialCommitInfo, CommitInfoRecord}

object CommitInfoToViewConverter {

  def partialCommitDetailsQuery = {
    CommitInfoRecord.select(_.id, _.sha, _.message, _.authorName, _.authorEmail, _.authorDate)
  }

  def tupleToCommitDetails(record: (ObjectId, String, String, String, String, Date)): PartialCommitInfo = {
    (PartialCommitInfo.apply _).tupled(record)
  }

  def toCommitViews(commits: List[PartialCommitInfo]) = {
    commits.map(toCommitView)
  }

  def toCommitView(commit: PartialCommitInfo) = {
    CommitView(commit.id.toString, commit.sha, commit.message, commit.authorName, commit.authorEmail, commit.date)
  }

}

