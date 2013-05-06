package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.reporting.views.{CommitView, CommitListView}

/**
 * Responsible for fetching commit list in read model.
 */
trait CommitFinder {

  def findCommitsToReviewForUser(userId: ObjectId): CommitListView
  def findAll(userId: ObjectId): CommitListView
  def findCommitInfoById(commitIdStr: String, userId: ObjectId): Either[String, CommitView]

}
