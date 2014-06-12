package com.softwaremill.codebrag.activities.finders

import com.softwaremill.codebrag.cache.RepositoriesCache
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.User

protected[finders] trait UserBranchAndRepoPreferences {

  protected def userDao: UserDAO
  protected def repoCache: RepositoriesCache

  protected def loadUser(userId: ObjectId) = userDao.findById(userId).getOrElse(throw new IllegalArgumentException("Invalid userId provided"))

  protected def findTargetRepoAndBranchNames(user: User, repoName: Option[String], branchName: Option[String]) = {
    val userRepo = repoName.getOrElse("bootzooka")  // TODO: load from user settings (like branch)
    val userBranch = branchName.getOrElse(user.settings.selectedBranch.getOrElse(repoDefaultBranch(userRepo)))
    (userRepo, userBranch)
  }

  private def repoDefaultBranch(repoName: String) = repoCache.getCheckedOutBranchShortName(repoName)

}
