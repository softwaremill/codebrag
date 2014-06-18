package com.softwaremill.codebrag.dao.browsingcontext

import com.softwaremill.codebrag.domain.UserBrowsingContext
import org.bson.types.ObjectId

trait UserBrowsingContextDAO {

  def save(context: UserBrowsingContext)
  def find(userId: ObjectId, repoName: String): Option[UserBrowsingContext]
  def findDefault(userId: ObjectId): Option[UserBrowsingContext]

}
