package com.softwaremill.codebrag.usecases

import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.usecases.assertions.UserAssertions
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{Authentication, User}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.licence.LicenceService
import com.softwaremill.scalaval.Validation._

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

class ModifyUserDetailsUseCase(protected val userDao: UserDAO, protected val licenceService: LicenceService) extends Logging {

  import UserAssertions._

  def execute(executorId: ObjectId, form: ModifyUserDetailsForm): Either[Errors, Unit] = {
    assertUserWithId(executorId, mustBeActive, mustBeAdmin)(userDao)
    val targetUser = loadUser(form.userId)
    validateUserDetails(executorId, targetUser, form).whenOk[Unit] {
      val modifiedUser = form.applyTo(targetUser)
      logger.debug(s"Validation passed, attempting to modify user $modifiedUser")
      userDao.modifyUser(modifiedUser)
    }
  }

  private def loadUser(userId: ObjectId) = userDao.findById(userId).getOrElse(throw new IllegalStateException(s"User $userId not found"))

  private def validateUserDetails(executorId: ObjectId, user: User, form: ModifyUserDetailsForm) = {
    val checkUserActive = rule("active") {
      (!form.newPassword.isDefined || (form.newPassword.isDefined && user.active), "Cannot set password for inactive user")
    }
    val changeOwnFlagsCheck = rule("userId") {
      val isModifyingFlags = form.admin.isDefined || form.active.isDefined
      (!isModifyingFlags || (isModifyingFlags && executorId != user.id), "Cannot modify own user")
    }
    val activeUsersLicenceLimitCheck = rule("active")(!activeUsersCountExceeded(user, form.active), "Licenced active users count exceeded")
    validate(checkUserActive, changeOwnFlagsCheck, activeUsersLicenceLimitCheck)
  }

  private def activeUsersCountExceeded(targetUser: User, activeOpt: Option[Boolean]) = {
    activeOpt.exists { newActiveFlag =>
      val currentStatusInactive = targetUser.active == false
      lazy val activeCountReached = userDao.countAllActive() == licenceService.maxUsers
      newActiveFlag && currentStatusInactive && activeCountReached
    }
  }
}
