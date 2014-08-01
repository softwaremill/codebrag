package com.softwaremill.codebrag.usecases.branches

import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.observedbranch.UserObservedBranchDAO
import com.softwaremill.codebrag.licence.LicenceService
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.scalaval.Validation


class RemoveBranchFromObserved(val userObservedBranchDao: UserObservedBranchDAO, licenceService: LicenceService) (implicit clock: Clock) extends Logging {

  def execute(userId: ObjectId, observedBranchId: ObjectId): Either[Validation.Errors, Unit]  = {
    validateBranchRemoval(userId, observedBranchId).whenOk {
      userObservedBranchDao.delete(observedBranchId)
    }
  }

  def validateBranchRemoval(userId: ObjectId, branchId: ObjectId) = {
    licenceService.interruptIfLicenceExpired()
    import com.softwaremill.scalaval.Validation._
    val branchObservedByUser = rule("branchObservedByUser") {
      val observedByUser = userObservedBranchDao.findAll(userId).exists(o => o.id == branchId)
      (observedByUser, "You're not watching this branch")
    }
    validate(branchObservedByUser)
  }

}