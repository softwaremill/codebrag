package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class User(id: ObjectId, authentication: Authentication, name: String, email: String, token: String)

object User {
  def apply(authentication: Authentication, name: String, email: String, token: String) = {
    new User(null, authentication, name, email, token)
  }
}

case class Authentication(provider: String, username: String, usernameLowerCase: String, token: String, salt: String)

object Authentication {
  def github(username: String, accessToken: String) = {
    Authentication("GitHub", username, username.toLowerCase, accessToken, "")
  }

  def basic(username: String, password: String) = {
    Authentication("Basic", username, username.toLowerCase, password, "")
  }
}

