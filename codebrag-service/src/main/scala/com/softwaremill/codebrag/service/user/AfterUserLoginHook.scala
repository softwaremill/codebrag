package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.service.commits.branches.ReviewedCommitsCache
import com.softwaremill.codebrag.service.data.UserJson
import org.bson.types.ObjectId

class AfterUserLoginHook(reviewedCommitsCache: ReviewedCommitsCache) {

  def postLogin(user: UserJson) {
    reviewedCommitsCache.loadUserReviewedCommitsToCache(new ObjectId(user.id))
  }

}
