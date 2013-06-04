package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.{CommitReviewTaskRecord, CommitInfoRecord}
import com.foursquare.rogue.LiftRogue._
import java.util.Date
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.CommitListView
import com.softwaremill.codebrag.dao.reporting.views.CommitView
import scala.Some
import com.softwaremill.codebrag.common.PagingCriteria

class MongoCommitFinder extends CommitFinder {

  private case class PartialCommitDetails(id: ObjectId, sha: String, message: String, authorName: String, date: Date)

  private def totalReviewTaskCount(userId: ObjectId): Int = {
    CommitReviewTaskRecord.where(_.userId eqs userId).count().toInt
  }

  override def findCommitsToReviewForUser(userId: ObjectId, paging: PagingCriteria) = {
    val commits = commitsToReviewFor(userId, paging)
    val count = totalReviewTaskCount(userId)
    CommitListView(commits.map(toDto(_)), count.toInt)
  }

  private def commitsToReviewFor(userId: ObjectId, paging: PagingCriteria) = {
    val userReviewTasks = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
    val commitIds = userReviewTasks.map(_.commitId.get).toSet
    val commitsFromDB = projectionQuery.where(_.id in commitIds).skip(paging.skip).limit(paging.limit).orderAsc(_.committerDate).fetch()
    commitsFromDB.map(commit => (PartialCommitDetails.apply _).tupled(commit))
  }

  override def findCommitInfoById(commitIdStr: String, userId: ObjectId) = {
    val commitId = new ObjectId(commitIdStr)
    val commitInfoOption = projectionQuery.where(_.id eqs commitId).get()
    commitInfoOption match {
      case Some(record) => {
        val commit = (PartialCommitDetails.apply _).tupled(record)
        val userReviewTasks = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
        val commitIds = userReviewTasks.map(_.commitId.get).toSet
        Right(markNotPendingReview(toDto(commit), commitIds))
      }
      case None => Left(s"No such commit $commitIdStr")
    }
  }

  private def toDto(record: PartialCommitDetails): CommitView = {
    CommitView(record.id.toString, record.sha, record.message, record.authorName, record.date)
  }

  private def projectionQuery = {
    CommitInfoRecord.select(_.id, _.sha, _.message, _.authorName, _.authorDate)
  }

  override def findAll(userId: ObjectId) = {
    val userReviewTasks = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
    val commitsFromDB = projectionQuery.orderAsc(_.committerDate).fetch()
    val commits = commitsFromDB.map(commit => (PartialCommitDetails.apply _).tupled(commit))
    val commitIds = userReviewTasks.map(_.commitId.get).toSet
    val count = if (userReviewTasks.isEmpty) 0
    else totalReviewTaskCount(userId)
    CommitListView(commits.map(toDto(_)).map(markNotPendingReview(_, commitIds)), count)
  }

  private def markNotPendingReview(commitView: CommitView, commitIdsPendingReview: Set[ObjectId]): CommitView = {
    if (commitIdsPendingReview.contains(new ObjectId(commitView.id)))
      commitView
    else
      commitView.copy(pendingReview = false)
  }
}