package com.softwaremill.codebrag.finders.user

import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{User, UserSettings}
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContext

case class LoggedInUserView(
  id: ObjectId,
  login: String,
  fullName: String,
  email: String,
  admin: Boolean,
  settings: UserSettings,
  browsingContext: UserBrowsingContext)

object LoggedInUserView {
   def apply(user: User, userContext: UserBrowsingContext) = {
     new LoggedInUserView(user.id, user.authentication.username, user.name, user.emailLowerCase, user.admin, user.settings, userContext)
   }
 }
