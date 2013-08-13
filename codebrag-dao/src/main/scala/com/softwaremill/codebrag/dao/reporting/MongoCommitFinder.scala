package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.{UserRecord, CommitReviewTaskRecord, CommitInfoRecord}
import com.foursquare.rogue.LiftRogue._
import java.util.Date
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.CommitListView
import com.softwaremill.codebrag.dao.reporting.views.CommitView
import com.softwaremill.codebrag.common.{PagingCriteria, SurroundingsCriteria}
import com.softwaremill.codebrag.domain.CommitAuthorClassification._
import com.softwaremill.codebrag.domain.UserLike
import com.typesafe.scalalogging.slf4j.Logging

class MongoCommitFinder extends CommitFinder with CommitReviewedByUserMarker with Logging {

  private case class PartialCommitDetails(id: ObjectId, sha: String, message: String, authorName: String,
                                          authorEmail: String, date: Date)

  def findAllCommits(paging: PagingCriteria, userId: ObjectId) = {
    val allCommitsIds = CommitInfoRecord.select(_.id).orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
    val commitsSlice = slicePageUsingCriteria(allCommitsIds, paging)
    markAsReviewed(toCommitViews(commitsSlice), userId)
  }

  def findCommitsToReviewForUser(userId: ObjectId, paging: PagingCriteria) = {
    val commitsIdsToReview =  commitsToReviewForUser(userId).map(_.commitId.get).toSet
    val commitsIdsSorted = CommitInfoRecord.select(_.id).where(_.id in commitsIdsToReview).orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
    val commitsSlice = slicePageUsingCriteria(commitsIdsSorted, paging)
    val count = commitsCountToReviewForUser(userId)
    CommitListView(toCommitViews(commitsSlice), count)
  }

  def findCommitInfoById(commitIdStr: String, userId: ObjectId) = {
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

  def findSurroundings(criteria: SurroundingsCriteria, userId: ObjectId) = {
    val allCommitsIds = CommitInfoRecord.select(_.id).orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
    def boundsForSurroundings(givenIndex: Int) = (givenIndex - criteria.loadLimit, givenIndex + criteria.loadLimit + 1)
    val commits = loadCommitsWithinBounds(allCommitsIds, Some(criteria.commitId), criteria.loadLimit, boundsForSurroundings)
    markAsReviewed(toCommitViews(commits), userId)
  }

  private def loadCommitsWithinBounds(allCommitsIds: List[ObjectId], commitId: Option[ObjectId], limit: Int, boundsFn: (Int => (Int, Int))) = {

    def loadCommits(boundsFn: (Int) => (Int, Int), indexOfGivenCommit: Int, allCommitsIds: List[ObjectId]): List[MongoCommitFinder.this.type#PartialCommitDetails] = {
      val bounds = boundsFn(indexOfGivenCommit)
      val commitsToLoad = allCommitsIds.slice(bounds._1, bounds._2)
      projectionQuery.where(_.id in commitsToLoad).orderAsc(_.committerDate).andAsc(_.authorDate).fetch().map(commit => (PartialCommitDetails.apply _).tupled(commit))
    }

    commitId match {
      case Some(id) => {
        val indexOfCommitId = allCommitsIds.indexOf(id)
        if(indexOfCommitId > -1) loadCommits(boundsFn, indexOfCommitId, allCommitsIds) else List.empty
      }
      case None => loadCommits(boundsFn, -1, allCommitsIds)
    }
  }

  private def slicePageUsingCriteria(commitsIds: List[ObjectId], criteria: PagingCriteria): List[PartialCommitDetails] = {
    if(criteria.maxCommitId.isDefined) {
      def boundsForPreviousCommits(givenIndex: Int) = (givenIndex - criteria.limit, givenIndex)
      loadCommitsWithinBounds(commitsIds, criteria.maxCommitId, criteria.limit, boundsForPreviousCommits)
    } else if(criteria.minCommitId.isDefined) {
      def boundsForNextCommits(givenIndex: Int) = (givenIndex + 1, givenIndex + criteria.limit + 1)
      loadCommitsWithinBounds(commitsIds, criteria.minCommitId, criteria.limit, boundsForNextCommits)
    } else {
      def boundsForNextCommits(givenIndex: Int) = (givenIndex + 1, givenIndex + criteria.limit + 1)
      loadCommitsWithinBounds(commitsIds, None, criteria.limit, boundsForNextCommits)
    }
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

  def findCommitsToReviewForUser(userId: ObjectId, paging: PagingCriteria) = {
    enhanceWithUserData(baseCommitFinder.findCommitsToReviewForUser(userId, paging))
  }

  def findCommitInfoById(commitIdStr: String, userId: ObjectId): Either[String, CommitView] = {
    baseCommitFinder.findCommitInfoById(commitIdStr, userId).right.map(enhanceWithUserData)
  }

  def findSurroundings(criteria: SurroundingsCriteria, userId: ObjectId) = {
    enhanceWithUserData(baseCommitFinder.findSurroundings(criteria, userId))
  }

  def findAllCommits(paging: PagingCriteria, userId: ObjectId) = {
    enhanceWithUserData(baseCommitFinder.findAllCommits(paging, userId))
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
