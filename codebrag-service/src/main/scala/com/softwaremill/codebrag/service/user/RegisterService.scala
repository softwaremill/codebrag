package com.softwaremill.codebrag.service.user

import com.typesafe.scalalogging.slf4j.Logging

class RegisterService extends Logging {
  def register(login: String, email: String, password: String): Either[String, Unit] = {
    logger.info(s"Trying to register $login")
    Left("not implemented")
  }
}
