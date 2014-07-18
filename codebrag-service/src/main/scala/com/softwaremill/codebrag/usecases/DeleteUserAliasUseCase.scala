package com.softwaremill.codebrag.usecases

import com.softwaremill.codebrag.dao.user.{UserDAO, UserAliasDAO}
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserAlias
import com.softwaremill.codebrag.usecases.validation.{Validation, ValidationErrors}

class DeleteUserAliasUseCase(val userAliasDao: UserAliasDAO) extends Logging {

  def execute(executorId: ObjectId, aliasId: ObjectId): Either[ValidationErrors, Unit]  = {
    validate(executorId, aliasId).whenNoErrors {
      userAliasDao.remove(aliasId)
    }
  }

  private def validate(executorId: ObjectId, aliasId: ObjectId): Validation = {
    val check = userAliasDao.findById(aliasId).map { a =>
      (a.userId != executorId, "This is not your alias", "alias")
    } getOrElse {
      (true, "Alias not found", "alias")
    }
    Validation(check)
  }
}
