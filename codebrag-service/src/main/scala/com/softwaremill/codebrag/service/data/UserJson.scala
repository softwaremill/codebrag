package com.softwaremill.codebrag.service.data

import com.softwaremill.codebrag.domain.User
import org.bson.types.ObjectId

case class UserJson(id: ObjectId, login: String, email:String, token: String)

object UserJson {
  def apply(user: User) = new UserJson(user.id, user.authentication.username, user.email, user.token)

  def apply(list: List[User]): List[UserJson] = {
    for (user <- list) yield UserJson(user)
  }

  def apply(userOpt: Option[User]): Option[UserJson] = {
    userOpt match {
      case Some(user) => new Some(UserJson(user))
      case _ => None
    }
  }
}
