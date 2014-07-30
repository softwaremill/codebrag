package com.softwaremill.codebrag.usecases

import com.softwaremill.codebrag.dao.user.UserAliasDAO
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.scalaval.Validation._

class DeleteUserAliasUseCase(val userAliasDao: UserAliasDAO) extends Logging {

  def execute(executorId: ObjectId, aliasId: ObjectId): Either[Errors, Unit]  = {
    validateAlias(executorId, aliasId).whenOk {
      userAliasDao.remove(aliasId)
    }
  }

  private def validateAlias(executorId: ObjectId, aliasId: ObjectId) = {
    val exists = rule("alias", haltOnFail = true) {
      val userRuleExists = userAliasDao.findById(aliasId).exists(_.userId == executorId)
      (userRuleExists, "You don't have such alias defined")

    }
    validate(exists)
  }
}
