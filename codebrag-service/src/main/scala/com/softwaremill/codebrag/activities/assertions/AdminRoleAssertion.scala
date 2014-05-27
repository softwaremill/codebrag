package com.softwaremill.codebrag.activities.assertions

import com.softwaremill.codebrag.dao.user.UserDAO
import org.bson.types.ObjectId
import com.softwaremill.codebrag.activities.exceptions.PermissionDeniedException

trait AdminRoleAssertion {

  protected def userDao: UserDAO

  def assertUserIsAdmin(userId: ObjectId) {
    userDao.findById(userId).map(_.admin) match {
      case Some(true) =>
      case Some(false) => throw new PermissionDeniedException(s"User $userId is not allowed to invite other users")
      case None => throw new IllegalStateException(s"User $userId not found")
    }
  }

}
