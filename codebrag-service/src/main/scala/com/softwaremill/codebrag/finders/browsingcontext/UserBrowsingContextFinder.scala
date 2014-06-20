package com.softwaremill.codebrag.finders.browsingcontext

import com.softwaremill.codebrag.dao.browsingcontext.UserBrowsingContextDAO
import com.softwaremill.codebrag.cache.RepositoriesCache
import com.softwaremill.codebrag.domain.UserBrowsingContext
import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging

class UserBrowsingContextFinder(val userBrowsingContextDao: UserBrowsingContextDAO, val repositoriesCache: RepositoriesCache) extends Logging {

  def findUserDefaultContext(userId: ObjectId) = {
    logger.debug(s"Searching for default browsing context for user $userId")
    val context = userBrowsingContextDao.findDefault(userId)
      .filter(context => repositoriesCache.hasRepo(context.repoName))
      .getOrElse(findSystemDefaultContext(userId))
    logger.debug(s"User context is $context")
    context
  }

  private def findSystemDefaultContext(userId: ObjectId) = {
    logger.debug(s"Default context not found, getting system default")
    val repoName = repositoriesCache.repoNames.head
    val branchName = repositoriesCache.getRepo(repoName).getCheckedOutBranchShortName
    logger.debug(s"Building system default context from $repoName and $branchName")
    UserBrowsingContext(userId, repoName, branchName)
  }
}
