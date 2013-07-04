package com.softwaremill.codebrag.service.user

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.domain.{User, Authentication}
import java.util.UUID

class RegisterService(userDao: UserDAO, newUserAdder: NewUserAdder) extends Logging {
  def register(login: String, email: String, password: String): Either[String, Unit] = {
    logger.info(s"Trying to register $login")

    val emailLowerCase = email.toLowerCase

    for {
      _ <- leftIfFound(userDao.findByLowerCasedLogin(login), "User with the given login already exists").right
      _ <- leftIfFound(userDao.findByEmail(emailLowerCase), "User with the given email already exists").right
    } yield {
      val user = User(Authentication.basic(login, password), login, emailLowerCase,
        UUID.randomUUID().toString, User.defaultAvatarUrl(emailLowerCase))

      newUserAdder.add(user)
    }
  }

  private def leftIfFound(userOpt: Option[User], msg: String) = {
    userOpt.map({user => Left(msg)}).getOrElse(Right())
  }
}
