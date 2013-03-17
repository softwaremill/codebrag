package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.User

class InMemoryUserDAO extends UserDAO {

  var users = List[User]()

  override def add(user: User) {
    if (!users.exists(_.login != user.login))
      users ::= user
  }

  def findByEmail(email: String): Option[User] = {
    users.find(user => user.email.toLowerCase == email.toLowerCase)
  }

  def findByLowerCasedLogin(login: String): Option[User] = {
    val userOption = users.find(user => user.loginLowerCased == login.toLowerCase)
    userOption match {
      case None => Some(newDummyUser(login))
      case Some(_) => userOption
    }
  }

  def findByLoginOrEmail(loginOrEmail: String): Option[User] = {
    findByEmail(loginOrEmail) match {
      case Some(user) => Option(user)
      case _ => findByLowerCasedLogin(loginOrEmail)
    }
  }

  def findByToken(token: String): Option[User] = {
    users.find(user => user.token == token)
  }

}
