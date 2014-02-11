package com.softwaremill.codebrag.dao.reviewtask

import com.softwaremill.codebrag.domain.CommitReviewTask
import org.bson.types.ObjectId

trait CommitReviewTaskDAO {

  def save(toReview: CommitReviewTask)

  def delete(task: CommitReviewTask)

  def commitsPendingReviewFor(userId: ObjectId): Set[ObjectId]
}
