package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.CommitReview
import org.bson.types.ObjectId

trait CommitReviewDAO {

  def findById(id: ObjectId): Option[CommitReview]
  def save(review: CommitReview)
}
