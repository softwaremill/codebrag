package com.softwaremill.codebrag.finders.commits

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging

protected[finders] trait UserLoader extends Logging {

  protected def userDao: UserDAO

  protected def loadUser(userId: ObjectId) = userDao.findById(userId).getOrElse(throw new IllegalArgumentException("Invalid userId provided"))

}
