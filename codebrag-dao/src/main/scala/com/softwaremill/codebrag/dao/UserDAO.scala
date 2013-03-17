package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.User
import java.util.UUID
import pl.softwaremill.common.util.RichString

trait UserDAO {

  protected def newDummyUser(login: String): User = {
    val token = UUID.randomUUID().toString
    val salt = RichString.generateRandom(16)
    val user = User(login, email = login + "@sml.com", plainPassword = login, salt, token)
    add(user)
    user
  }

  def add(user: User)

  def findByEmail(email: String): Option[User]

  def findByLowerCasedLogin(login: String): Option[User]

  def findByLoginOrEmail(loginOrEmail: String): Option[User]

  def findByToken(token: String): Option[User]

}
