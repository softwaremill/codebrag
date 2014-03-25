package com.softwaremill.codebrag.service.user

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.{UserSettings, User, Authentication}
import java.util.UUID
import com.softwaremill.codebrag.service.invitations.InvitationService
import com.softwaremill.codebrag.service.notification.NotificationService
import com.softwaremill.codebrag.dao.user.UserDAO

class RegisterService(userDao: UserDAO, newUserAdder: NewUserAdder, invitationService: InvitationService, notificationService: NotificationService) extends Logging {

  def firstRegistration: Boolean = userDao.findAll().isEmpty

  def register(login: String, email: String, password: String, invitationCode: String): Either[String, Unit] = {
    logger.info(s"Trying to register $login")
    val emailLowerCase = email.toLowerCase
    if (firstRegistration) {
      registerUser(login, emailLowerCase, password)
    } else {
      registerUserWithInvitation(login, emailLowerCase, password, invitationCode)
    }
  }

  private def registerUserWithInvitation(login: String, emailLowerCase: String, password: String, invitationCode: String): Either[String, Unit] = {
    for {
      _ <- leftIfTrue(invitationCode.isEmpty, "To register in Codebrag you need a registration link. Ask another Codebrag user to send you one.").right
      _ <- leftIfTrue(!invitationService.verify(invitationCode), "The registration link is not valid or was already used. Ask your friend for another one.").right
      _ <- leftIfSome(userDao.findByLowerCasedLogin(login), "User with the given login already exists").right
      _ <- leftIfSome(userDao.findByEmail(emailLowerCase), "User with the given email already exists").right
    } yield {
      registerUser(login, emailLowerCase, password)
    }
  }

  private def registerUser(login: String, emailLowerCase: String, password: String): Either[String, Unit] = {
    val user = User(Authentication.basic(login, password), login, emailLowerCase,
      UUID.randomUUID().toString, UserSettings.defaultAvatarUrl(emailLowerCase))
    val addedUser = newUserAdder.add(user)
    notificationService.sendWelcomeNotification(addedUser)
    Right()
  }

  private def leftIfTrue(cond: Boolean, message: String) = {
    if (cond) {
      Left(message)
    } else {
      Right()
    }
  }

  private def leftIfSome[A](userOpt: Option[A], msg: String) = {
    userOpt.map({
      user => Left(msg)
    }).getOrElse(Right())
  }
}
