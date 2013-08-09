package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.{UserRecord, CommitReviewTaskRecord, CommitInfoRecord}
import com.foursquare.rogue.LiftRogue._
import java.util.Date
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.CommitListView
import com.softwaremill.codebrag.dao.reporting.views.CommitView
import com.softwaremill.codebrag.common.{LoadMoreCriteria, LoadSurroundingsCriteria}
import com.softwaremill.codebrag.domain.CommitAuthorClassification._
import com.softwaremill.codebrag.domain.UserLike
import com.typesafe.scalalogging.slf4j.Logging

class MongoCommitFinder extends CommitFinder with CommitReviewedByUserMarker with Logging {

  private case class PartialCommitDetails(id: ObjectId, sha: String, message: String, authorName: String,
                                          authorEmail: String, date: Date)

  override def findCommitsToReviewForUser(userId: ObjectId, paging: LoadMoreCriteria) = {
    val commits = findCommitsToReview(userId, paging)
    val count = commitsCountToReviewForUser(userId)
    CommitListView(toCommitViews(commits), count)
  }

  override def findCommitInfoById(commitIdStr: String, userId: ObjectId) = {
    val commitId = new ObjectId(commitIdStr)
    val commitInfoOption = projectionQuery.where(_.id eqs commitId).get()
    commitInfoOption match {
      case Some(record) => {
        val commit = tupleToCommitDetails(record)
        Right(markAsReviewed(toCommitView(commit), userId))
      }
      case None => Left(s"No such commit ${commitId.toString}")
    }
  }

  override def findAll(userId: ObjectId) = {
    val commitsFromDB = projectionQuery.orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
    val commits = commitsFromDB.map(commit => tupleToCommitDetails(commit))
    markAsReviewed(toCommitViews(commits), userId)
  }

  def findSurroundings(criteria: LoadSurroundingsCriteria, userId: ObjectId) = {
    val allCommitsIds = CommitInfoRecord.select(_.id).orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
    val indexOfGivenCommit = allCommitsIds.indexOf(criteria.commitId)
    if(indexOfGivenCommit > -1) {
      val lowerBound = indexOfGivenCommit - criteria.loadLimit
      val upperBound = indexOfGivenCommit + criteria.loadLimit
      val surroundingsIds = allCommitsIds.slice(lowerBound, upperBound + 1)
      val commits = projectionQuery.where(_.id in surroundingsIds).orderAsc(_.committerDate).andAsc(_.authorDate).fetch().map(commit => (PartialCommitDetails.apply _).tupled(commit))
      Right(markAsReviewed(toCommitViews(commits), userId))
    } else {
      Left(s"No such commit ${criteria.commitId.toString}")
    }
  }

  private def findCommitsToReview(userId: ObjectId, paging: LoadMoreCriteria) = {
    val commitIds = commitsToReviewForUser(userId).map(_.commitId.get).toSet
    val ids = CommitInfoRecord.select(_.id).where(_.id in commitIds).orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
    paging.lastCommitId match {
      case Some(id) => {
        val indexOfLast = ids.indexOf(id)
        if(indexOfLast > -1) commitsSlice(indexOfLast + 1, paging.limit, ids) else List.empty
      }
      case _ => commitsSlice(0, paging.limit, ids)
    }
  }

  private def commitsSlice(startingIndex: Int, limit: Int, ids: List[ObjectId]) = {
    val maxIndex = startingIndex + limit
    val newList = ids.slice(startingIndex, maxIndex)
    val commitsFromDB = projectionQuery.where(_.id in newList).orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
    commitsFromDB.map(commit => tupleToCommitDetails(commit))
  }

  private def commitsToReviewForUser(userId: ObjectId) = {
    CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
  }

  private def commitsCountToReviewForUser(userId: ObjectId) = {
    CommitReviewTaskRecord.where(_.userId eqs userId).count().toInt
  }

  private def tupleToCommitDetails(record: (ObjectId, String, String, String, String, Date)): MongoCommitFinder.this.type#PartialCommitDetails = {
    (PartialCommitDetails.apply _).tupled(record)
  }

  private def projectionQuery = {
    CommitInfoRecord.select(_.id, _.sha, _.message, _.authorName, _.authorEmail, _.authorDate)
  }

  private def toCommitViews(commits: List[PartialCommitDetails]) = {
    commits.map(toCommitView)
  }

  private def toCommitView(commit: PartialCommitDetails) = {
    CommitView(commit.id.toString, commit.sha, commit.message, commit.authorName, commit.authorEmail, commit.date)
  }

}

trait CommitReviewedByUserMarker {

  def markAsReviewed(commitsViews: List[CommitView], userId: ObjectId) = {
    val remainingToReview = commitsPendingReviewFor(userId)
    val marked = commitsViews.map(markIfReviewed(_, remainingToReview))
    CommitListView(marked, remainingToReview.size)
  }

  def markAsReviewed(commitView: CommitView, userId: ObjectId) = {
    val remainingToReview = commitsPendingReviewFor(userId)
    markIfReviewed(commitView, remainingToReview)
  }

  private def markIfReviewed(commitView: CommitView, remainingToReview: Set[ObjectId]) = {
    if (remainingToReview.contains(new ObjectId(commitView.id)))
      commitView
    else
      commitView.copy(pendingReview = false)
  }

  private def commitsPendingReviewFor(userId: ObjectId) = {
    val userReviewTasks = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
    userReviewTasks.map(_.commitId.get).toSet
  }

}


class MongoCommitWithAuthorDetailsFinder(baseCommitFinder: CommitFinder) extends CommitFinder with CommitViewWithUserDataEnhancer {

  def findCommitsToReviewForUser(userId: ObjectId, paging: LoadMoreCriteria) = {
    enhanceWithUserData(baseCommitFinder.findCommitsToReviewForUser(userId, paging))
  }

  def findAll(userId: ObjectId): CommitListView = {
    enhanceWithUserData(baseCommitFinder.findAll(userId))
  }

  def findCommitInfoById(commitIdStr: String, userId: ObjectId): Either[String, CommitView] = {
    baseCommitFinder.findCommitInfoById(commitIdStr, userId).right.map(enhanceWithUserData)
  }

  def findSurroundings(criteria: LoadSurroundingsCriteria, userId: ObjectId) = {
    baseCommitFinder.findSurroundings(criteria, userId).right.map(enhanceWithUserData)
  }

}

trait CommitViewWithUserDataEnhancer {

  private case class PartialUserDetails(name: String, email: String, avatarUrl: String)

  private object PartialUserDetails {
    implicit object UserLikePartialUserDetails extends UserLike[PartialUserDetails] {
      def userFullName(userLike: PartialUserDetails) = userLike.name
      def userEmail(userLike: PartialUserDetails) = userLike.email
    }
  }

  def enhanceWithUserData(commit: CommitView) = {
    val commitAuthorOpt = findCommitAuthor(commit)
    val commitAuthorAvatarUrl = authorAvatar(commitAuthorOpt)
    commit.copy(authorAvatarUrl = commitAuthorAvatarUrl)
  }

  def enhanceWithUserData(commitsList: CommitListView) = {
    val authors = findCommitsAuthors(commitsList)
    val commitsWithAvatars = commitsList.commits.map(commit => {
      val commitAuthorAvatarUrl = authorAvatar(authors.find(commitAuthoredByUser(commit, _)))
      commit.copy(authorAvatarUrl = commitAuthorAvatarUrl)
    })
    commitsList.copy(commits = commitsWithAvatars)
  }

  private def findCommitAuthor(commit: CommitView): Option[PartialUserDetails] = findCommitsAuthors(List(commit)).headOption

  private def findCommitsAuthors(commitsList: CommitListView): List[PartialUserDetails] = findCommitsAuthors(commitsList.commits)

  private def findCommitsAuthors(commits: List[CommitView]): List[PartialUserDetails] = {
    val userNames = commits.map(_.authorName).toSet
    val userEmails = commits.map(_.authorEmail).toSet
    val usersFromDB = userProjectionQuery.or(_.where(_.name in userNames), _.where(_.email in userEmails)).fetch()
    usersFromDB.map(user => (PartialUserDetails.apply _).tupled(user))
  }

  private def userProjectionQuery = UserRecord.select(_.name, _.email, _.avatarUrl)

  private def authorAvatar(authorOpt: Option[PartialUserDetails]): String = {
    authorOpt match {
      case Some(author) => author.avatarUrl
      case None => "" // no avatar if unknown user? handle that on frontend
    }
  }

}
