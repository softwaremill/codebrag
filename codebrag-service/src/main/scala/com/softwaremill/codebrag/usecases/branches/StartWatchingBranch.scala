package com.softwaremill.codebrag.usecases.branches

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserWatchedBranch
import com.softwaremill.codebrag.dao.branch.WatchedBranchesDao
import com.softwaremill.codebrag.licence.LicenceService
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.scalaval.Validation




class StartWatchingBranch(val watchedBranchesDao: WatchedBranchesDao, licenceService: LicenceService) (implicit clock: Clock) extends Logging {

  def execute(userId: ObjectId, form: WatchedBranchForm): Either[Validation.Errors, UserWatchedBranch]  = {
    val newBranch = UserWatchedBranch(new ObjectId, userId, form.repoName, form.branchName)
    validateBranchToWatch(newBranch).whenOk[UserWatchedBranch] {
      watchedBranchesDao.save(newBranch)
      newBranch
    }
  }

  def validateBranchToWatch(newBranch: UserWatchedBranch) = {
    licenceService.interruptIfLicenceExpired()
    import com.softwaremill.scalaval.Validation._
    val branchAlreadyWatched = rule("repoBranch") {
      val exists = watchedBranchesDao.findAll(newBranch.userId).exists(o => o.repoName == newBranch.repoName && o.branchName == newBranch.branchName)
      (!exists, "You're already watching this branch")
    }
    validate(branchAlreadyWatched)
  }

}
