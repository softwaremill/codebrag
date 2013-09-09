package com.softwaremill.codebrag.service.user

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.domain.{User, Authentication}
import java.util.UUID
import com.softwaremill.codebrag.service.invitations.InvitationService

class RegisterService(userDao: UserDAO, newUserAdder: NewUserAdder, invitationService: InvitationService) extends Logging {

  def firstRegistration: Boolean = {
    userDao.findAll() match {
      case Nil => true
      case _ => false
    }


  }

  def register(login: String, email: String, password: String, invitationCode: String): Either[String, Unit] = {
    logger.info(s"Trying to register $login")
    userDao.findAll() match {
      case Nil => registerUser(login, email, password)
      case _ => registerUserWithInvitation(login, email, password, invitationCode)
    }
  }

  private def registerUserWithInvitation(login: String, emailLowerCase: String, password: String, invitationCode: String): Either[String, Unit] = {
    for {
      _ <- lestIfTrue(invitationCode.isEmpty, "Invitation code cannot be blank").right
      _ <- lestIfTrue(!invitationService.verify(invitationCode), "Invitation for code: " + invitationCode + " doesn't exist").right
      _ <- leftIfSome(userDao.findByLowerCasedLogin(login), "User with the given login already exists").right
      _ <- leftIfSome(userDao.findByEmail(emailLowerCase), "User with the given email already exists").right
    } yield {
      val user = User(Authentication.basic(login, password), login, emailLowerCase,
        UUID.randomUUID().toString, User.defaultAvatarUrl(emailLowerCase))

      newUserAdder.add(user)
      invitationService.expire(invitationCode)
    }
  }

  private def registerUser(login: String, email: String, password: String): Either[String, Unit] = {
    val emailLowerCase = email.toLowerCase
    val user = User(Authentication.basic(login, password), login, emailLowerCase,
      UUID.randomUUID().toString, User.defaultAvatarUrl(emailLowerCase))
    newUserAdder.add(user)
    Right()
  }

  private def lestIfTrue(cond: Boolean, message: String) = {
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
