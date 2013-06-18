package com.softwaremill.codebrag.dao.events

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Event
import com.softwaremill.codebrag.domain.UserLike

case class NewUserRegistered(id: ObjectId, login: String, fullName: String, email: String) extends Event

object NewUserRegistered {
  implicit object UserLikeNewUserRegisteredEvent extends UserLike[NewUserRegistered]{
    def userFullName(userLike: NewUserRegistered): String = userLike.fullName
  }
}