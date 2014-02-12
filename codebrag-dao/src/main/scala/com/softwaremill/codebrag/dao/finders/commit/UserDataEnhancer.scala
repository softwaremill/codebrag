package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.dao.reporting.views.CommitListView
import com.softwaremill.codebrag.dao.reporting.views.CommitView
import com.softwaremill.codebrag.domain.CommitAuthorClassification._
import com.softwaremill.codebrag.dao.user.{UserDAO, PartialUserDetails}

trait UserDataEnhancer {
  def userDAO: UserDAO

  def enhanceWithUserData(commit: CommitView) = {
    val commitAuthorOpt = findCommitAuthor(commit)
    val commitAuthorAvatarUrl = authorAvatar(commitAuthorOpt)
    commit.copy(authorAvatarUrl = commitAuthorAvatarUrl)
  }

  def enhanceWithUserData(commitsList: CommitListView) = {
    val authors = findCommitsAuthors(commitsList.commits)
    val commitsWithAvatars = commitsList.commits.map(commit => {
      val commitAuthorAvatarUrl = authorAvatar(authors.find(commitAuthoredByUser(commit, _)))
      commit.copy(authorAvatarUrl = commitAuthorAvatarUrl)
    })
    commitsList.copy(commits = commitsWithAvatars)
  }

  private def findCommitAuthor(commit: CommitView): Option[PartialUserDetails] = findCommitsAuthors(List(commit)).headOption

  private def findCommitsAuthors(commits: List[CommitView]): Iterable[PartialUserDetails] = {
    val userNames = commits.map(_.authorName).toSet
    val userEmails = commits.map(_.authorEmail).toSet
    userDAO.findPartialUserDetails(userNames, userEmails)
  }

  private def authorAvatar(authorOpt: Option[PartialUserDetails]): String = {
    authorOpt match {
      case Some(author) => author.avatarUrl
      case None => "" // no avatar if unknown user? handle that on frontend
    }
  }

}
