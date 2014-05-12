package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.service.data.UserJson
import org.bson.types.ObjectId
import com.softwaremill.codebrag.cache.UserReviewedCommitsCache

class AfterUserLogin(reviewedCommitsCache: UserReviewedCommitsCache) {

  def postLogin(user: UserJson) {
    reviewedCommitsCache.loadUserDataToCache(new ObjectId(user.id))
  }

}
