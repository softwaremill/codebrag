package com.softwaremill.codebrag.service.data

import com.softwaremill.codebrag.domain.User

case class UserJson(id: String, login: String, fullName: String, email:String, token: String, avatarUrl: String)

object UserJson {
  def apply(user: User) = {
    new UserJson(user.id.toString, user.authentication.username, user.name, user.email, user.token, user.avatarUrl)
  }

  def apply(list: List[User]): List[UserJson] = {
    for (user <- list) yield UserJson(user)
  }
}
