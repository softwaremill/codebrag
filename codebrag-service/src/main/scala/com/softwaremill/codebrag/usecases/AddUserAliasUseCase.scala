package com.softwaremill.codebrag.usecases

import com.softwaremill.codebrag.dao.user.{UserDAO, UserAliasDAO}
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.UserAlias
import com.softwaremill.codebrag.usecases.validation.{ValidationErrors, Validation}
import org.apache.commons.validator.routines.EmailValidator

class AddUserAliasUseCase(val userAliasDao: UserAliasDAO, val userDao: UserDAO) extends Logging {

  def execute(userId: ObjectId, alias: String): Either[ValidationErrors, UserAlias]  = {
    val newAlias = UserAlias(userId, alias.toLowerCase)
    validate(newAlias).whenNoErrors[UserAlias] {
      userAliasDao.save(newAlias)
      newAlias
    }
  }

  private def validate(alias: UserAlias): Validation = {
    if(EmailValidator.getInstance().isValid(alias.alias)) {
      val primaryEmailExists = (userDao.findByEmail(alias.alias).isDefined, "This email is already defined as primary", "alias")
      val existingAliasOpt = userAliasDao.findByAlias(alias.alias)
      val existsForThisUserCheck = (existingAliasOpt.exists(_.userId == alias.userId), "You have such alias already defined", "alias")
      val existsForOtherUserCheck = (existingAliasOpt.exists(_.userId != alias.userId), "Such alias is already defined for other user", "alias")
      Validation(primaryEmailExists, existsForThisUserCheck, existsForOtherUserCheck)
    } else {
      Validation((true, "Invalid email provided", "alias"))
    }
  }
}
