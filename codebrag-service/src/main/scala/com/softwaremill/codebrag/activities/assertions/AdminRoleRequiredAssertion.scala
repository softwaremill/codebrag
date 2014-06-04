package com.softwaremill.codebrag.activities.assertions

import com.softwaremill.codebrag.dao.user.UserDAO
import org.bson.types.ObjectId
import com.softwaremill.codebrag.activities.assertions.AdminRoleRequiredException

class AdminRoleRequiredException(msg: String = "Admin role required") extends RuntimeException(msg)

trait AdminRoleRequiredAssertion {

  protected def userDao: UserDAO

  def assertIsAdmin(userId: ObjectId) {
    userDao.findById(userId).map(_.admin) match {
      case Some(true) =>
      case Some(false) => throw new AdminRoleRequiredException
      case None => throw new IllegalStateException(s"User $userId not found")
    }
  }

}
