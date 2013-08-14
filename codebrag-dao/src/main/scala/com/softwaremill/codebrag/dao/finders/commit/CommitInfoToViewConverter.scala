package com.softwaremill.codebrag.dao.finders.commit

import org.bson.types.ObjectId
import java.util.Date
import com.softwaremill.codebrag.dao.CommitInfoRecord
import com.softwaremill.codebrag.dao.reporting.views.CommitView
import com.foursquare.rogue.LiftRogue._

object CommitInfoToViewConverter {

  case class PartialCommitDetails(id: ObjectId, sha: String, message: String, authorName: String, authorEmail: String, date: Date)

  def partialCommitDetailsQuery = {
    CommitInfoRecord.select(_.id, _.sha, _.message, _.authorName, _.authorEmail, _.authorDate)
  }

  def tupleToCommitDetails(record: (ObjectId, String, String, String, String, Date)): PartialCommitDetails = {
    (PartialCommitDetails.apply _).tupled(record)
  }

  def toCommitViews(commits: List[PartialCommitDetails]) = {
    commits.map(toCommitView)
  }

  def toCommitView(commit: PartialCommitDetails) = {
    CommitView(commit.id.toString, commit.sha, commit.message, commit.authorName, commit.authorEmail, commit.date)
  }

}

