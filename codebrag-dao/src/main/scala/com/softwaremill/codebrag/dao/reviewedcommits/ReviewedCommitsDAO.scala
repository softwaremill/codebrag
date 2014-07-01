package com.softwaremill.codebrag.dao.reviewedcommits

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.ReviewedCommit
import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.joda.time.DateTimeZone

trait ReviewedCommitsDAO {

  def storeReviewedCommit(commit: ReviewedCommit)

  def allReviewedByUser(userId: ObjectId, repoName: String): Set[ReviewedCommit]

}