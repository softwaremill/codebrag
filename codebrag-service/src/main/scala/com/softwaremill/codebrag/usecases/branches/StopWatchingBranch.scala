package com.softwaremill.codebrag.usecases.branches

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.branch.WatchedBranchesDao
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.scalaval.Validation
import com.softwaremill.codebrag.domain.UserWatchedBranch


class StopWatchingBranch(val watchedBranchesDao: WatchedBranchesDao) (implicit clock: Clock) extends Logging {

  def execute(userId: ObjectId, form: WatchedBranchForm): Either[Validation.Errors, Unit]  = {
    val watchedBranchOpt = findUserWatchedBranch(userId, form)
    validateBranchRemoval(userId, watchedBranchOpt).whenOk {
      watchedBranchOpt.foreach(b => watchedBranchesDao.delete(b.id))
    }
  }

  def validateBranchRemoval(userId: ObjectId, branchOpt: Option[UserWatchedBranch]) = {
    import com.softwaremill.scalaval.Validation._
    val branchWatchedByUser = rule("branchWatchedByUser")(branchOpt.isDefined, "You're not watching this branch")
    validate(branchWatchedByUser)
  }

  private def findUserWatchedBranch(userId: ObjectId, form: WatchedBranchForm) = {
    watchedBranchesDao.findAll(userId).find(o => o.repoName == form.repoName && o.branchName == form.branchName)
  }

}