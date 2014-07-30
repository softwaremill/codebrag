package com.softwaremill.codebrag.usecases.observedbranches

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserObservedBranch
import com.softwaremill.codebrag.dao.observedbranch.UserObservedBranchDAO
import com.softwaremill.codebrag.licence.LicenceService
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.scalaval.Validation


case class NewObservedBranch(repoName: String, branchName: String)

class AddBranchToObserved(val userObservedBranchDao: UserObservedBranchDAO, licenceService: LicenceService) (implicit clock: Clock) extends Logging {

  def execute(userId: ObjectId, form: NewObservedBranch): Either[Validation.Errors, UserObservedBranch]  = {
    val newBranch = UserObservedBranch(new ObjectId, userId, form.repoName, form.branchName)
    validateBranchToObserve(newBranch).whenOk[UserObservedBranch] {
      userObservedBranchDao.save(newBranch)
      newBranch
    }
  }

  def validateBranchToObserve(newBranch: UserObservedBranch) = {
    licenceService.interruptIfLicenceExpired()
    import com.softwaremill.scalaval.Validation._
    val branchAlreadyObserved = rule("repoBranch") {
      val exists = userObservedBranchDao.findAll(newBranch.userId).exists(o => o.repoName == newBranch.repoName && o.branchName == newBranch.branchName)
      (!exists, "You're already watching this branch")
    }
    validate(branchAlreadyObserved)
  }

}
