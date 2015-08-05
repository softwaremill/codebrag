package com.softwaremill.codebrag.usecases.user

import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.usecases.assertions.UserAssertions
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{Authentication, User}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.scalaval.Validation._

case class DeleteUserForm(userId: ObjectId) {

}

class DeleteUserUseCase(protected val userDao: UserDAO) extends Logging {

  import UserAssertions._

  def execute(executorId: ObjectId, form: DeleteUserForm): Either[Errors, Unit] = {
    assertUserWithId(executorId, mustBeActive, mustBeAdmin)(userDao)
    val targetUser = loadUser(form.userId)
    validateUserDetails(executorId, targetUser, form).whenOk[Unit] {      
      logger.debug(s"Validation passed, attempting to delete user $targetUser")
      userDao.delete(form.userId)
    }
  }

  private def loadUser(userId: ObjectId) = userDao.findById(userId).getOrElse(throw new IllegalStateException(s"User $userId not found"))

  private def validateUserDetails(executorId: ObjectId, user: User, form: DeleteUserForm) = {   
    val changeOwnFlagsCheck = rule("userId") {
      val isDeleteFlags = user.admin || user.active
      (!isDeleteFlags || (isDeleteFlags && executorId != user.id), "Cannot delete own user")
    }
    validate(changeOwnFlagsCheck)
  }

}
