package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.{CommitView, CommitListView}
import com.softwaremill.codebrag.common.{PagingCriteria, SurroundingsCriteria}

trait CommitFinder {

  def findCommitsToReviewForUser(userId: ObjectId, paging: PagingCriteria): CommitListView
  def findCommitInfoById(commitIdStr: String, userId: ObjectId): Either[String, CommitView]
  def findSurroundings(criteria: SurroundingsCriteria, userId: ObjectId): CommitListView
  def findAllCommits(paging: PagingCriteria, userId: ObjectId): CommitListView

}
