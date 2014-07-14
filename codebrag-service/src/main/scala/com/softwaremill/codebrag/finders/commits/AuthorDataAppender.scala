package com.softwaremill.codebrag.finders.commits

import com.softwaremill.codebrag.domain.CommitAuthorClassification._
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.finders.views.{CommitView, CommitListView}
import com.softwaremill.codebrag.domain.PartialUserDetails

trait AuthorDataAppender {

  def userDao: UserDAO

  def addAutorData(commit: CommitView) = {
    val commitAuthorOpt = findCommitAuthor(commit)
    val commitAuthorAvatarUrl = authorAvatar(commitAuthorOpt)
    commit.copy(authorAvatarUrl = commitAuthorAvatarUrl)
  }

  def addAuthorData(commitsList: CommitListView) = {
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
    userDao.findPartialUserDetails(userNames, userEmails)
  }

  private def authorAvatar(authorOpt: Option[PartialUserDetails]): String = {
    authorOpt match {
      case Some(author) => author.avatarUrl
      case None => "" // no avatar if unknown user? handle that on frontend
    }
  }

}
