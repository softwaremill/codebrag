package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.{UserRecord, CommitReviewTaskRecord, CommitInfoRecord}
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
    val commits = findCommitsToReview(userId, paging)
    val count = totalReviewTaskCount(userId)
    CommitListView(toCommitViews(commits), count.toInt)
  }

  private def findCommitsToReview(userId: ObjectId, paging: PagingCriteria) = {
    val commitIds = findPendingCommitsIds(userId)
    val commitsFromDB = projectionQuery.where(_.id in commitIds).skip(paging.skip).limit(paging.limit).orderAsc(_.committerDate).fetch()
    commitsFromDB.map(commit => (PartialCommitDetails.apply _).tupled(commit))
  }

  override def findCommitInfoById(commitIdStr: String, userId: ObjectId) = {
    val commitId = new ObjectId(commitIdStr)
    val commitInfoOption = projectionQuery.where(_.id eqs commitId).get()
    commitInfoOption match {
      case Some(record) => {
        val commit = (PartialCommitDetails.apply _).tupled(record)
        Right(markNotPendingReview(toCommitView(commit), findPendingCommitsIds(userId)))
      }
      case None => Left(s"No such commit $commitIdStr")
    }
  }

  private def findPendingCommitsIds(userId: ObjectId) = {
    val userReviewTasks = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
    userReviewTasks.map(_.commitId.get).toSet
  }

  private def projectionQuery = {
    CommitInfoRecord.select(_.id, _.sha, _.message, _.authorName, _.authorDate)
  }

  override def findAll(userId: ObjectId) = {
    val commitsFromDB = projectionQuery.orderAsc(_.committerDate).fetch()
    val commits = commitsFromDB.map(commit => (PartialCommitDetails.apply _).tupled(commit))
    val pendingCommitsIds = findPendingCommitsIds(userId)
    val count = if (pendingCommitsIds.isEmpty) 0 else totalReviewTaskCount(userId)
    CommitListView(toCommitViews(commits).map(markNotPendingReview(_, pendingCommitsIds)), count)
  }

  private def markNotPendingReview(commitView: CommitView, commitIdsPendingReview: Set[ObjectId]): CommitView = {
    if (commitIdsPendingReview.contains(new ObjectId(commitView.id)))
      commitView
    else
      commitView.copy(pendingReview = false)
  }

  private def toCommitViews(commits: List[PartialCommitDetails]) = {
    commits.map(toCommitView(_))
  }

  private def toCommitView(commit: PartialCommitDetails) = {
    CommitView(commit.id.toString, commit.sha, commit.message, commit.authorName, commit.date)
  }

}



class MongoCommitWithAuthorDetailsFinder(baseCommitFinder: CommitFinder) extends CommitFinder {

  private case class PartialUserDetails(name: String, avatarUrl: String)

  def findCommitsToReviewForUser(userId: ObjectId, paging: PagingCriteria): CommitListView = {
    val commitsViews = baseCommitFinder.findCommitsToReviewForUser(userId, paging)
    val commitsAuthors = findCommitsAuthors(commitsViews)
    buildCommitsViewList(commitsViews, commitsAuthors)
  }

  def findAll(userId: ObjectId): CommitListView = {
    val commitsViews = baseCommitFinder.findAll(userId)
    val commitsAuthors = findCommitsAuthors(commitsViews)
    buildCommitsViewList(commitsViews, commitsAuthors)
  }

  def findCommitInfoById(commitIdStr: String, userId: ObjectId): Either[String, CommitView] = {
    baseCommitFinder.findCommitInfoById(commitIdStr, userId) match {
      case Right(commitView) => {
        val commitAuthor = findCommitAuthor(commitView)
        Right(buildCommitView(commitView, commitAuthor))
      }
      case Left(msg) => Left(msg)
    }
  }

  private def findCommitAuthor(commit: CommitView) = {
    UserRecord.select(_.name, _.avatarUrl).where(_.name eqs commit.authorName).get() match {
      case Some(author) => Some((PartialUserDetails.apply _).tupled(author))
      case None => None
    }
  }

  private def buildCommitView(commit: CommitView, authorOpt: Option[PartialUserDetails]) = {
    val commitAuthorAvatarUrl = authorAvatar(authorOpt)
    commit.copy(authorAvatarUrl = commitAuthorAvatarUrl)
  }

  private def findCommitsAuthors(commitsList: CommitListView) = {
    val userNames = commitsList.commits.map(_.authorName)
    val usersFromDB = UserRecord.select(_.name, _.avatarUrl).where(_.name in userNames).fetch()
    usersFromDB.map(user => (PartialUserDetails.apply _).tupled(user))
  }

  private def buildCommitsViewList(commitsList: CommitListView, authors: List[PartialUserDetails]) = {
    val commitsWithAvatars = commitsList.commits.map(commit => {
      val commitAuthorAvatarUrl = authorAvatar(authors.find(_.name == commit.authorName))
      commit.copy(authorAvatarUrl = commitAuthorAvatarUrl)
    })
    commitsList.copy(commits = commitsWithAvatars)
  }

  private def authorAvatar(authorOpt: Option[PartialUserDetails]): String = {
    authorOpt match {
      case Some(author) => author.avatarUrl
      case None => "" // no avatar if unknown user? handle that on frontend
    }
  }

}