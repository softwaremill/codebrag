package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.{CommitReviewTaskRecord, CommitInfoRecord}
import com.foursquare.rogue.LiftRogue._
import java.util.Date
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.{CommitView, CommitListView}
import com.softwaremill.codebrag.common.PagingCriteria
import com.foursquare.rogue._
import com.softwaremill.codebrag.dao.reporting.views.CommitListView
import com.softwaremill.codebrag.dao.reporting.views.CommitView
import scala.Some
import com.foursquare.rogue.Query
import com.softwaremill.codebrag.common.PagingCriteria

class MongoCommitFinder extends CommitFinder {

  private def totalReviewTaskCount(userId: ObjectId): Int = {
    CommitReviewTaskRecord.where(_.userId eqs userId).count().toInt
  }

  override def findCommitsToReviewForUser(userId: ObjectId, paging: PagingCriteria) = {
    val userReviewTasks = CommitReviewTaskRecord.where(_.userId eqs userId).skip(paging.skip).limit(paging.limit).fetch()
    val commitIds = userReviewTasks.map(_.commitId.get).toSet
    val query = projectionQuery.where(_.id in commitIds).orderAsc(_.committerDate)
    val commits = query.fetch()
    val count = totalReviewTaskCount(userId)
    CommitListView(commits.map(recordToDto(_)), count.toInt)
  }

  override def findCommitInfoById(commitIdStr: String, userId: ObjectId) = {
    val commitId = new ObjectId(commitIdStr)
    val commitInfoOption = projectionQuery.where(_.id eqs commitId).get()
    commitInfoOption match {
      case Some(record) => {
        val userReviewTasks = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
        val commitIds = userReviewTasks.map(_.commitId.get).toSet
        Right(markNotPendingReview(recordToDto(record), commitIds))
      }
      case None => Left(s"No such commit $commitIdStr")
    }
  }

  private def recordToDto(recordData: (ObjectId, String, String, String, String, Date)): CommitView = {
    CommitView(recordData._1.toString, recordData._2, recordData._3,
      recordData._4, recordData._5, recordData._6)
  }

  private def projectionQuery = {
    CommitInfoRecord.select(_.id, _.sha, _.message, _.authorName, _.committerName, _.authorDate)
  }

  override def findAll(userId: ObjectId) = {
    val userReviewTasks = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
    val commits = projectionQuery.orderAsc(_.committerDate).fetch()
    val commitIds = userReviewTasks.map(_.commitId.get).toSet
    val count = if (userReviewTasks.isEmpty) 0
    else totalReviewTaskCount(userId)
    CommitListView(commits.map(recordToDto(_)).map(markNotPendingReview(_, commitIds)), count)
  }

  private def markNotPendingReview(commitView: CommitView, commitIdsPendingReview: Set[ObjectId]): CommitView = {
    if (commitIdsPendingReview.contains(new ObjectId(commitView.id)))
      commitView
    else
      commitView.copy(pendingReview = false)
  }
}