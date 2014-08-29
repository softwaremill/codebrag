package com.softwaremill.codebrag.usecases.user

import com.softwaremill.codebrag.licence.LicenceService
import com.softwaremill.codebrag.service.invitations.InvitationService
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging

// TODO: test!
class UserRegistrationValidator(
  licenceService: LicenceService,
  invitationService: InvitationService,
  userDao: UserDAO) extends Logging {

  def validateRegistration(form: RegistrationForm, firstRegistration: Boolean) = {
    import com.softwaremill.scalaval.Validation._
    if(firstRegistration) {
      validate(rules = List.empty)
    } else {
      val licence = rule("licence", haltOnFail = true)(licenceService.maxUsers > userDao.countAllActive(), "Unable to register new user - maximum number of licensed users exceeded")
      val invitationCodePresent = rule("invitationCode", haltOnFail = true)(form.invitationCode.nonEmpty, "To register in Codebrag you need a registration link. Ask another Codebrag user to send you one.")
      val invitationCodeValid = rule("invitationCode", haltOnFail = true)(invitationService.verify(form.invitationCode), "The registration link is not valid or was already used. Ask your friend for another one.")
      val userNameAvailable = rule("userName")(userDao.findByLowerCasedLogin(form.login).isEmpty, "User with the given login already exists")
      val userEmailAvailable = rule("userEmail")(userDao.findByEmail(form.email).isEmpty, "User with the given email already exists")
      validate(licence, invitationCodePresent, invitationCodeValid, userNameAvailable, userEmailAvailable)
    }
  }

}
