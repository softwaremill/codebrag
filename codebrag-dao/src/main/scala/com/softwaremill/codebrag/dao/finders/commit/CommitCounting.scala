package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.dao.reporting.views.CommitView

object CommitCounting {
  def countOlderCommits(commitIds: List[String], pagedCommitIds: List[CommitView]) = {
    if (pagedCommitIds.isEmpty) 0
    else commitIds.take(commitIds.indexOf(pagedCommitIds.head.id)).size
  }

  def countNewerCommits(commitIds: List[String], pagedCommitIds: List[CommitView]) = {
    if (pagedCommitIds.isEmpty) 0
    else {
      val lastId = pagedCommitIds.reverse.head.id
      commitIds.takeRight(commitIds.size - commitIds.indexOf(lastId) - 1).size
    }
  }

}
