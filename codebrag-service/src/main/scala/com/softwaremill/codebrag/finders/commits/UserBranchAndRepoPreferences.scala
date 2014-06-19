package com.softwaremill.codebrag.finders.commits

import com.softwaremill.codebrag.cache.RepositoriesCache
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.User
import com.typesafe.scalalogging.slf4j.Logging

protected[finders] trait UserBranchAndRepoPreferences extends Logging {

  protected def userDao: UserDAO
  protected def repoCache: RepositoriesCache

  protected def loadUser(userId: ObjectId) = userDao.findById(userId).getOrElse(throw new IllegalArgumentException("Invalid userId provided"))

  protected def findTargetRepoAndBranchNames(user: User, repoName: Option[String], branchName: Option[String]) = {
    val userRepo = repoName.getOrElse {
      logger.debug(s"No repository provided, using first one from the list of known repos")
      repoCache.repoNames.head
    }
    val userBranch = branchName.getOrElse(user.settings.selectedBranch.getOrElse(repoDefaultBranch(userRepo)))
    (userRepo, userBranch)
  }

  private def repoDefaultBranch(repoName: String) = repoCache.getCheckedOutBranchShortName(repoName)

}
