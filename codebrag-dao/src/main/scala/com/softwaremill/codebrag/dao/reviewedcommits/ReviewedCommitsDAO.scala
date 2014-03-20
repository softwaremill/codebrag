package com.softwaremill.codebrag.dao.reviewedcommits

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.ReviewedCommit
import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.joda.time.DateTimeZone

trait ReviewedCommitsDAO {

  def storeReviewedCommit(commit: ReviewedCommit)

  def allReviewedByUser(userId: ObjectId): Set[ReviewedCommit]

}

class InMemoryReviewedCommitsDAO extends ReviewedCommitsDAO {

  private val storage = new scala.collection.mutable.HashSet[ReviewedCommit]

  override def allReviewedByUser(userId: ObjectId) = storage.filter(_.userId == userId).toSet

  override def storeReviewedCommit(commit: ReviewedCommit) = storage.add(commit)

}