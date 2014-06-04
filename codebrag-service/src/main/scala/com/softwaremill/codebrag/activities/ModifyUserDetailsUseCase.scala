package com.softwaremill.codebrag.activities

import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.activities.assertions.AdminRoleAssertion
import org.bson.types.ObjectId
import com.softwaremill.codebrag.activities.validation.{Validation, ValidationErrors}
import com.softwaremill.codebrag.domain.{Authentication, User}
import com.typesafe.scalalogging.slf4j.Logging

case class ModifyUserDetailsForm(userId: ObjectId, newPassword: Option[String], admin: Option[Boolean], active: Option[Boolean]) {

  def applyTo(user: User) = {
    val modifiedAuth = newPassword.map { newPass =>
      buildNewAuth(user.authentication.username, newPass)
    } getOrElse user.authentication
    user.copy(
      admin = admin.getOrElse(user.admin),
      active = active.getOrElse(user.active),
      authentication = modifiedAuth
    )
  }

  protected def buildNewAuth(username: String, password: String) = Authentication.basic(username, password)

}

class ModifyUserDetailsUseCase(protected val userDao: UserDAO) extends AdminRoleAssertion with Logging {

  def execute(executorId: ObjectId, form: ModifyUserDetailsForm): Either[ValidationErrors, Unit] = {
    assertUserIsAdmin(executorId)
    val targetUser = loadUser(form.userId)
    validate(executorId, targetUser, form).whenNoErrors[Unit] {
      val modifiedUser = form.applyTo(targetUser)
      logger.debug(s"Validation passed, attempting to modify user $modifiedUser")
      userDao.modifyUser(modifiedUser)
    }
  }

  private def loadUser(userId: ObjectId) = userDao.findById(userId).getOrElse(throw new IllegalStateException(s"User $userId not found"))

  private def validate(executorId: ObjectId, user: User, form: ModifyUserDetailsForm): Validation = {
    val inactiveUserCheck = (!user.active && form.newPassword.isDefined, "Cannot set password for inactive user", "active")
    val changeOwnFlagsCheck = (executorId == user.id && (form.admin.isDefined || form.active.isDefined), "Cannot modify own user", "userId")
    Validation(inactiveUserCheck, changeOwnFlagsCheck)
  }

}
