package com.softwaremill.codebrag.finders.browsingcontext

import com.softwaremill.codebrag.dao.browsingcontext.UserBrowsingContextDAO
import com.softwaremill.codebrag.cache.RepositoriesCache
import com.softwaremill.codebrag.domain.UserBrowsingContext
import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging

class UserBrowsingContextFinder(val userBrowsingContextDao: UserBrowsingContextDAO, val repositoriesCache: RepositoriesCache) extends Logging {

  def findUserDefaultContext(userId: ObjectId) = {
    logger.debug(s"Searching for default browsing context for user $userId")
    userBrowsingContextDao.findDefault(userId).getOrElse(findSystemDefaultContext(userId))
  }


  private def findSystemDefaultContext(userId: ObjectId) = {
    logger.debug(s"Default context not found, getting default")
    val repoName = repositoriesCache.repoNames.head
    val branchName = repositoriesCache.getRepo(repoName).getCheckedOutBranchShortName
    logger.debug(s"Building context from $repoName and $branchName")
    UserBrowsingContext(userId, repoName, branchName)
  }
}
