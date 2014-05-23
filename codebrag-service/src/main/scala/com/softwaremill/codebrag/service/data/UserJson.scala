package com.softwaremill.codebrag.service.data

import com.softwaremill.codebrag.domain.{UserSettings, User}
import org.bson.types.ObjectId

case class UserJson(id: String, login: String, fullName: String, email:String, token: String, admin: Boolean, settings: UserSettings) {
  def idAsObjectId = new ObjectId(id)
}

object UserJson {
  def apply(user: User) = {
    new UserJson(user.id.toString, user.authentication.username, user.name, user.emailLowerCase, user.token, user.admin, user.settings)
  }

  def apply(list: List[User]): List[UserJson] = {
    for (user <- list) yield UserJson(user)
  }
}
