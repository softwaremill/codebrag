package com.softwaremill.codebrag.activities.finders

import com.softwaremill.codebrag.cache.BranchCommitsCache
import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain.User

protected[finders] trait UserAndBranch {

  protected def userDao: UserDAO
  protected def repoCache: BranchCommitsCache

  protected def loadUser(userId: ObjectId) = userDao.findById(userId).getOrElse(throw new IllegalArgumentException("Invalid userId provided"))

  protected def determineBranch(user: User, branchName: Option[String]) = {
    branchName.getOrElse(user.settings.selectedBranch.getOrElse(repoCache.getCheckedOutBranchShortName))
  }

}
