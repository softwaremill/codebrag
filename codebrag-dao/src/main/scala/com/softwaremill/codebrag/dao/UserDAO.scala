package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{Authentication, User}
import java.util.UUID
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.{EventBus, Utils}
import com.softwaremill.codebrag.dao.events.NewUserRegistered

trait UserDAO {

  val eventBus: EventBus

  protected def createAndSaveDummyUser(login: String): User = {
    val token = UUID.randomUUID().toString
    val user = User(new ObjectId, Authentication.basic(login, login), login,  s"$login@sml.com", token, "")
    add(user)
    eventBus.publish(NewUserRegistered(user.id, login, user.name, user.email))
    findByLoginOrEmail(login).get
  }

  def add(user: User)

  def findAll(): List[User]

  def findById(userId: ObjectId): Option[User]

  def findByEmail(email: String): Option[User]

  def findByLowerCasedLogin(login: String): Option[User]

  def findByLoginOrEmail(loginOrEmail: String): Option[User]

  def findByToken(token: String): Option[User]

  def findByUserName(userName: String): Option[User]

  def changeAuthentication(id: ObjectId, authentication: Authentication)

}
