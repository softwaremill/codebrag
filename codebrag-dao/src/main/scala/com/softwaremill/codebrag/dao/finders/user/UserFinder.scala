package com.softwaremill.codebrag.dao.finders.user

import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.User

case class ManagedUsersListView(users: List[ManagedUserView])
case class ManagedUserView(userId: String, email: String, name: String, active: Boolean, admin: Boolean)

class UserFinder(userDao: UserDAO) {

  def findAllAsManagedUsers(): ManagedUsersListView = ManagedUsersListView(userDao.findAll().map(toManagedUser).sortBy(_.email))

  private def toManagedUser(user: User) = ManagedUserView(user.id.toString, user.emailLowerCase, user.name, user.active, user.admin)

}