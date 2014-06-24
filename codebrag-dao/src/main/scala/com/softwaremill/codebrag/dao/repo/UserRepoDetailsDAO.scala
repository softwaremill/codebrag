package com.softwaremill.codebrag.dao.repo

import com.softwaremill.codebrag.domain.UserRepoDetails
import org.bson.types.ObjectId

trait UserRepoDetailsDAO {

  def save(context: UserRepoDetails)
  def find(userId: ObjectId, repoName: String): Option[UserRepoDetails]
  def findDefault(userId: ObjectId): Option[UserRepoDetails]

}
