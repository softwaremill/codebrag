package com.softwaremill.codebrag.finders.user

import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.{UserBrowsingContext, UserSettings, User}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.service.data.UserJson
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.browsingcontext.UserBrowsingContextDAO
import com.softwaremill.codebrag.cache.RepositoriesCache

case class ManagedUsersListView(users: List[ManagedUserView])
case class ManagedUserView(userId: ObjectId, email: String, name: String, active: Boolean, admin: Boolean)


case class LoggedInUserView(
  id: ObjectId,
  login: String,
  fullName: String,
  email: String,
  admin: Boolean,
  settings: UserSettings,
  browsingContext: UserBrowsingContext)

object LoggedInUserView {
  def apply(user: UserJson, userContext: UserBrowsingContext) = {
    new LoggedInUserView(user.idAsObjectId, user.login, user.fullName, user.email, user.admin, user.settings, userContext)
  }
}



class UserFinder(userDao: UserDAO, userBrowsingContextDao: UserBrowsingContextDAO, repositoriesCache: RepositoriesCache) extends Logging {

  def findAllAsManagedUsers(): ManagedUsersListView = ManagedUsersListView(userDao.findAll().map(toManagedUser).sortBy(_.email))

  private def toManagedUser(user: User) = ManagedUserView(user.id, user.emailLowerCase, user.name, user.active, user.admin)

  def findLoggedInUser(user: UserJson): LoggedInUserView = {
    val userContext = findUserDefaultContext(user)
    LoggedInUserView(user, userContext)
  }

  private def findUserDefaultContext(user: UserJson) = {
    logger.debug(s"Searching for default browsing context for user ${user.login}")
    userBrowsingContextDao.findDefault(user.idAsObjectId).getOrElse {
      logger.debug(s"Default context not found, getting default")
      val repoName = repositoriesCache.repoNames.head
      val branchName = repositoriesCache.getRepo(repoName).getCheckedOutBranchShortName
      logger.debug(s"Building context from $repoName and $branchName")
      UserBrowsingContext(user.idAsObjectId, repoName, branchName)
    }
  }

}