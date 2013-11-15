package com.softwaremill.codebrag.dao.events

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Event
import com.softwaremill.codebrag.domain.{User, UserLike}

case class NewUserRegistered(id: ObjectId, login: String, fullName: String, email: String) extends Event

object NewUserRegistered {

  def apply(user: User) = {
    new NewUserRegistered(user.id, user.authentication.usernameLowerCase, user.name, user.email)
  }

  implicit object UserLikeNewUserRegisteredEvent extends UserLike[NewUserRegistered]{
    def userFullName(userLike: NewUserRegistered) = userLike.fullName
    def userEmail(userLike: NewUserRegistered) = userLike.email
  }
}