package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.service.commits.branches.UserReviewedCommitsCache
import com.softwaremill.codebrag.service.data.UserJson
import org.bson.types.ObjectId

class AfterUserLoginHook(reviewedCommitsCache: UserReviewedCommitsCache) {

  def postLogin(user: UserJson) {
    reviewedCommitsCache.loadUserDataToCache(new ObjectId(user.id))
  }

}
