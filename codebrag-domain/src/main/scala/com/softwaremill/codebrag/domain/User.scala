package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Utils
import pl.softwaremill.common.util.RichString

case class User(id: ObjectId, authentication: Authentication, name: String, email: String, token: String, avatarUrl: String)

object User {
  def apply(authentication: Authentication, name: String, email: String, token: String, avatarUrl: String) = {
    new User(null, authentication, name, email, token, avatarUrl)
  }

  def defaultAvatarUrl(email: String): String = {
    s"http://www.gravatar.com/avatar/${Utils.md5(email)}.png"
  }

  implicit object UserLikeRegularUser extends UserLike[User] {
    override def userFullName(userLike: User) = userLike.name
  }
}

trait UserLike[T] {
  def userFullName(userLike: T): String
}

case class Authentication(provider: String, username: String, usernameLowerCase: String, token: String, salt: String)

object Authentication {
  def github(username: String, accessToken: String) = {
    Authentication("GitHub", username, username.toLowerCase, accessToken, "")
  }

  def basic(username: String, password: String) = {
    val salt = RichString.generateRandom(16)
    Authentication("Basic", username, username.toLowerCase, encryptPassword(password, salt), salt)
  }

  def encryptPassword(password: String, salt: String): String = {
    Utils.sha256(password, salt)
  }

  def passwordsMatch(plainPassword: String, authentication: Authentication): Boolean = {
    authentication.token.equals(encryptPassword(plainPassword, authentication.salt))
  }

}

