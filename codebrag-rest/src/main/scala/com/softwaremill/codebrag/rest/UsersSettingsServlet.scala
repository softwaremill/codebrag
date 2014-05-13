package com.softwaremill.codebrag.rest

import org.scalatra._
import com.softwaremill.codebrag.service.user.Authenticator
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.activities.{IncomingSettings, ChangeUserSettingsUseCase}

class UsersSettingsServlet(val authenticator: Authenticator, userDao: UserDAO, changeUserSettings: ChangeUserSettingsUseCase) extends JsonServletWithAuthentication with Logging {

  get("/") {
    haltIfNotAuthenticated()
    userDao.findById(user.idAsObjectId) match {
      case Some(user) => Map("userSettings" -> user.settings)
      case None => NotFound
    }
  }

  put("/") {
    val newSettings = IncomingSettings(extractOpt[Boolean]("emailNotificationsEnabled"), extractOpt[Boolean]("dailyUpdatesEmailEnabled"), extractOpt[Boolean]("appTourDone"), extractOpt[String]("selectedBranch"))
    changeUserSettings.execute(user.idAsObjectId, newSettings).left.map { err =>
      halt(400, Map("error" -> err))
    }
  }

}
