package com.softwaremill.codebrag.dao.events

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.{Hookable, StatisticEvent, Clock, Event}
import com.softwaremill.codebrag.domain.{User, UserLike}
import org.joda.time.DateTime

case class NewUserRegistered(
    id: ObjectId,
    login: String,
    fullName: String,
    email: String
  )(implicit clock: Clock) extends Event with StatisticEvent with Hookable {

  val hookName = "new-user-registered"

  def eventType = NewUserRegistered.EventType

  def timestamp: DateTime = clock.nowUtc

  def userId = id

  def toEventStream: String = s"New user $fullName was registered"

}

object NewUserRegistered {

  val EventType = "UserRegistered"

  def apply(user: User)(implicit clock: Clock) = {
    new NewUserRegistered(user.id, user.authentication.usernameLowerCase, user.name, user.emailLowerCase)(clock)
  }

  implicit object UserLikeNewUserRegisteredEvent extends UserLike[NewUserRegistered] {

    def userFullName(userLike: NewUserRegistered) = userLike.fullName

    def userEmails(userLike: NewUserRegistered) = Set(userLike.email)

  }

}
