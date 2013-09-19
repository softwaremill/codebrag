package com.softwaremill.codebrag.dao.finders.commit

import com.softwaremill.codebrag.dao.reporting.views.CommitView

object CommitCounting {
  def countOlderCommits(allIdsToReview: List[String], pagedIdsToReview: List[CommitView]) = {
    if (pagedIdsToReview.isEmpty) 0
    else allIdsToReview.take(allIdsToReview.indexOf(pagedIdsToReview.head.id)).size
  }

  def countNewerCommits(allIdsToReview: List[String], pagedIdsToReview: List[CommitView]) = {
    if (pagedIdsToReview.isEmpty) 0
    else {
      val lastId = pagedIdsToReview.reverse.head.id
      allIdsToReview.takeRight(allIdsToReview.size - allIdsToReview.indexOf(lastId) - 1).size
    }
  }

}
