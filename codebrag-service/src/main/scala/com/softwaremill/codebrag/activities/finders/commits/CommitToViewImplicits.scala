package com.softwaremill.codebrag.activities.finders.commits

import com.softwaremill.codebrag.domain.PartialCommitInfo
import com.softwaremill.codebrag.dao.finders.views.CommitView

object CommitToViewImplicits {

  implicit def partialCommitListToCommitViewList(commits: List[PartialCommitInfo]): List[CommitView] = {
    commits.map(partialCommitToCommitView)
  }

  implicit def partialCommitToCommitView(commit: PartialCommitInfo): CommitView = {
    CommitView(commit.id.toString, commit.sha, commit.message, commit.authorName, commit.authorEmail, commit.date.toDate)
  }

}
