package com.softwaremill.codebrag.dao.observedbranch

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserObservedBranch


trait UserObservedBranchDAO {

  def save(branch: UserObservedBranch)
  def delete(id: ObjectId)
  def findAll(userId: ObjectId): Set[UserObservedBranch]

}
