package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Utils
import pl.softwaremill.common.util.RichString

case class User(id: ObjectId, authentication: Authentication, name: String, email: String, token: String, avatarUrl: String)

object User {
  def apply(authentication: Authentication, name: String, email: String, token: String, avatarUrl: String) = {
    new User(null, authentication, name, email, token, avatarUrl)
  }
}

case class Authentication(provider: String, username: String, usernameLowerCase: String, token: String, salt: String, avatarUrl: String)

object Authentication {
  def github(username: String, accessToken: String, avatarUrl: String) = {
    Authentication("GitHub", username, username.toLowerCase, accessToken, "", avatarUrl)
  }

  def basic(username: String, password: String) = {
    val salt = RichString.generateRandom(16)
    Authentication("Basic", username, username.toLowerCase, encryptPassword(password, salt), salt, Utils.defaultAvatarUrl(username))
  }

  def encryptPassword(password: String, salt: String): String = {
    Utils.sha256(password, salt)
  }

  def passwordsMatch(plainPassword: String, authentication: Authentication): Boolean = {
    authentication.token.equals(encryptPassword(plainPassword, authentication.salt))
  }
}

