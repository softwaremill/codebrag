package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.dao.reporting.views.CommitView

object OutOfPageCommitCounter {
  def countOlderCommits(commitIds: List[String], pagedCommitIds: List[CommitView]) = {
    pagedCommitIds.headOption match {
      case None => 0
      case Some(firstCommit) => commitIds.take(commitIds.indexOf(firstCommit.id)).size
    }
  }

  def countNewerCommits(commitIds: List[String], pagedCommitIds: List[CommitView]) = {
    pagedCommitIds.reverse.headOption match {
      case None => 0
      case Some(lastCommit) => commitIds.takeRight(commitIds.size - commitIds.indexOf(lastCommit.id) - 1).size
    }
  }

}
