package com.softwaremill.codebrag.usecases

import com.softwaremill.codebrag.dao.user.{UserDAO, UserAliasDAO}
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserAlias
import org.apache.commons.validator.routines.EmailValidator
import com.softwaremill.scalaval.Validation._

class AddUserAliasUseCase(val userAliasDao: UserAliasDAO, val userDao: UserDAO) extends Logging {

  def execute(userId: ObjectId, alias: String): Either[Errors, UserAlias]  = {
    val newAlias = UserAlias(userId, alias.toLowerCase)
    validateNewAlias(newAlias).whenOk[UserAlias] {
      userAliasDao.save(newAlias)
      newAlias
    }
  }

  private def validateNewAlias(alias: UserAlias) = {
    val emailValid = rule("alias", haltOnFail = true)(EmailValidator.getInstance().isValid(alias.alias), "Invalid email provided")
    val primaryEmailExists = rule("alias")(userDao.findByEmail(alias.alias).isEmpty, "This email is already defined as primary")
    val existingAliasOpt = userAliasDao.findByAlias(alias.alias)
    val existsForThisUserCheck = rule("alias")(!existingAliasOpt.exists(_.userId == alias.userId), "You have such alias already defined")
    val existsForOtherUserCheck = rule("alias")(!existingAliasOpt.exists(_.userId != alias.userId), "Such alias is already defined for other user")
    validate(emailValid, primaryEmailExists, existsForThisUserCheck, existsForOtherUserCheck)
  }
}
