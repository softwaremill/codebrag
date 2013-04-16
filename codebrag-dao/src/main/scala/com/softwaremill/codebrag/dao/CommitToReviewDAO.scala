package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.CommitToReview

trait CommitToReviewDAO {

  def save(toReview: CommitToReview)

}
