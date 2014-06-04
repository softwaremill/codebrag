package com.softwaremill.codebrag.activities.assertions

import com.softwaremill.codebrag.domain.User
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.user.UserDAO

class AdminRoleRequiredException(msg: String = "Admin role required to perform this action") extends RuntimeException(msg)
class ActiveUserStatusRequiredException(msg: String = "User must be active to perform this action") extends RuntimeException(msg)

/*
  Check required conditions and throw meaningful exception when not met
  Can queue up several conditions to be checked with "failing fast" way
 */
object UserAssertions {

  def mustBeAdmin(user: User) = if(!user.admin) throw new AdminRoleRequiredException
  def mustBeActive(user: User) = if(!user.active) throw new ActiveUserStatusRequiredException

  def assertUserWithId(userId: ObjectId, checks:(User => Unit)*)(implicit userDao: UserDAO) = {
    val user = userDao.findById(userId).getOrElse(throw new IllegalStateException(s"User $userId not found"))
    checks.foreach(check => check(user))
  }

  def assertUser(user: User, checks:(User => Unit)*) = checks.foreach(check => check(user))

}