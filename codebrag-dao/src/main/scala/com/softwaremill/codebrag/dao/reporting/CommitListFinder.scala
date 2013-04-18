package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId

/**
 * Responsible for fetching commit list in read model.
 */
trait CommitListFinder {

  def findCommitsToReviewForUser(userId: ObjectId): CommitListDTO

  def findCommitInfoById(commitId: String): Either[String, CommitListItemDTO]

}
