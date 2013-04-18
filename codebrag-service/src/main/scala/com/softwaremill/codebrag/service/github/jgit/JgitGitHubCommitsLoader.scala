package com.softwaremill.codebrag.service.github.jgit

import com.softwaremill.codebrag.service.github.GitHubCommitsLoader
import com.softwaremill.codebrag.domain.CommitInfo
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConversions._

class JgitGitHubCommitsLoader(jGitFacade: JgitFacade, internalDirTree: InternalGitDirTree, converter: JgitLogConverter) extends GitHubCommitsLoader with Logging {

  def loadMissingCommits(repoOwner: String, repoName: String): List[CommitInfo] = {

    val remotePath = createGitHubPath(repoOwner, repoName)
    val localPath = internalDirTree.getPath(repoOwner, repoName)

    val logCommand = if (!internalDirTree.containsRepo(repoOwner, repoName))
      jGitFacade.clone(remotePath, localPath).log()
    else
    {
      val previousHead = jGitFacade.getHeadId(localPath)
      val git = jGitFacade.pull(localPath)
      val headAfterPull = jGitFacade.getHeadId(localPath)
      git.log.addRange(previousHead, headAfterPull)
    }
    converter.toCommitInfos(logCommand.call().toList, logCommand.getRepository)
  }


  def createGitHubPath(repoOwner: String, repoName: String): String = {
    s"https://github.com/$repoOwner/$repoName.git"
  }
}