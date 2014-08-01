package com.softwaremill.codebrag.dao.branch

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserWatchedBranch


trait WatchedBranchesDao {

  def save(branch: UserWatchedBranch)
  def delete(id: ObjectId)
  def findAll(userId: ObjectId): Set[UserWatchedBranch]

}
