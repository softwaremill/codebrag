package com.softwaremill.codebrag.service.user

import com.softwaremill.codebrag.dao.UserDAO
import com.softwaremill.codebrag.service.data.UserJson
import com.softwaremill.codebrag.domain.{Authentication, User}
import java.util.UUID
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.github.{CommitReviewTaskGeneratorActions, CommitReviewTaskGenerator}
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.common.EventBus

class Authenticator(userDAO: UserDAO, eventBus: EventBus, reviewTaskGenerator: CommitReviewTaskGeneratorActions) {

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    currentOrNewUser(userDAO.findByLoginOrEmail(login), login)
  }

  def authenticateWithToken(token: String): Option[UserJson] = {
    userDAO.findByToken(token).map(UserJson(_))
  }

  def findByLogin(login: String): Option[UserJson] = {
    currentOrNewUser(userDAO.findByLowerCasedLogin(login), login)
  }

  private def currentOrNewUser(userOpt: Option[User], login: String) = {
    val actualUser = userOpt match {
      case Some(_) => userOpt
      case None => Some(createAndSaveDummyUser(login))
    }
    actualUser.map(UserJson(_))
  }

  private def createAndSaveDummyUser(login: String) = {
    val token = UUID.randomUUID().toString
    val user = User(new ObjectId, Authentication.basic(login, login), login, s"$login@sml.com", token, "")
    userDAO.add(user)
    val userRegisteredEvent = NewUserRegistered(user.id, user.authentication.usernameLowerCase, user.name, user.email)
    reviewTaskGenerator.handleNewUserRegistered(userRegisteredEvent)
    eventBus.publish(userRegisteredEvent)
    user
  }

}
