package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.dao.UserRecord
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.dao.reporting.views.CommitListView
import com.softwaremill.codebrag.dao.reporting.views.CommitView
import com.softwaremill.codebrag.domain.CommitAuthorClassification._
import com.softwaremill.codebrag.domain.UserLike


trait UserDataEnhancer {

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

  def enhanceWithUserData(commits: List[CommitView]) = {
    val authors = findCommitsAuthors(commits)
    commits.map(commit => {
      val commitAuthorAvatarUrl = authorAvatar(authors.find(commitAuthoredByUser(commit, _)))
      commit.copy(authorAvatarUrl = commitAuthorAvatarUrl)
    })
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
