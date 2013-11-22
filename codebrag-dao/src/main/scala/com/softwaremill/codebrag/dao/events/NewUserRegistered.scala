package com.softwaremill.codebrag.dao.events

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.{Clock, Event}
import com.softwaremill.codebrag.domain.{User, UserLike}
import org.joda.time.DateTime

case class NewUserRegistered(id: ObjectId, login: String, fullName: String, email: String)(implicit clock: Clock) extends Event {

  def timestamp: DateTime = clock.currentDateTimeUTC

  def userId: Option[ObjectId] = Some(id)

  def toEventStream: String = s"New user $fullName was registered"

}

object NewUserRegistered {

  def apply(user: User)(implicit clock: Clock) = {
    new NewUserRegistered(user.id, user.authentication.usernameLowerCase, user.name, user.email)(clock)
  }

  implicit object UserLikeNewUserRegisteredEvent extends UserLike[NewUserRegistered] {

    def userFullName(userLike: NewUserRegistered) = userLike.fullName

    def userEmail(userLike: NewUserRegistered) = userLike.email

  }

}
