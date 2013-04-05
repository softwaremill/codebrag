package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{Authentication, User}
import java.util.UUID
import org.bson.types.ObjectId

trait UserDAO {

  protected def createAndSaveDummyUser(login: String): User = {
    val token = UUID.randomUUID().toString
    val user = User(Authentication.basic(login, login), login, s"$login@sml.com", token)
    add(user)
    user
  }

  def add(user: User)

  def findById(userId: ObjectId): Option[User]

  def findByEmail(email: String): Option[User]

  def findByLowerCasedLogin(login: String): Option[User]

  def findByLoginOrEmail(loginOrEmail: String): Option[User]

  def findByToken(token: String): Option[User]

  def findByUserName(userName: String): Option[User]

  def changeAuthentication(id:ObjectId, authentication:Authentication)

}
