package com.softwaremill.codebrag.usecases.observedbranches

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserObservedBranch
import com.softwaremill.codebrag.dao.observedbranch.UserObservedBranchDAO

class FindUserObservedBranches(val userObservedBranchDao: UserObservedBranchDAO) extends Logging {

  type UserObservables = Map[String, Seq[UserObservedBranch]]

  def execute(userId: ObjectId): UserObservables = {
    userObservedBranchDao.findAll(userId)
      .groupBy(_.repoName)
      .map(o => (o._1, o._2.toSeq.sortBy(_.branchName)))
  }

}
