package com.softwaremill.codebrag.service.commits.jgit

import java.nio.file.Path
import com.softwaremill.codebrag.service.commits.{SvnRepoData, RepoData}
import org.eclipse.jgit.lib.ObjectId
import scala.sys.process._
import org.eclipse.jgit.api.LogCommand
import com.typesafe.scalalogging.slf4j.Logging
import org.apache.commons.lang3.SystemUtils
import java.io.File

class GitSvnRepoUpdater(jGitFacade: JgitFacade) extends RepoUpdater with Logging {
  def cloneFreshRepo(localPath: Path, repoData: RepoData): LogCommand = {
    checkoutSvnRepo(repoData.asInstanceOf[SvnRepoData], localPath)
    jGitFacade.gitRepo(localPath).log()
  }


  def checkoutSvnRepo(svnRepoData: SvnRepoData, localPath: Path) {
    if (isOsWindows) {
      createParentDirectoryIfNeeded(localPath.toFile)
    }
    if (svnRepoData.username.isEmpty) {
      prepareCommand(s"echo ${svnRepoData.password}") #| s"git svn clone ${svnRepoData.remoteUri} --quiet ${localPath.toString}" !< ProcessLogger(logger info _)
    } else {
      prepareCommand(s"echo ${svnRepoData.password}") #| s"git svn clone ${svnRepoData.remoteUri} --quiet --username ${svnRepoData.username} ${localPath.toString}" !< ProcessLogger(logger info _)
    }
    logger.debug("SVN repo checked out")
  }

  private def createParentDirectoryIfNeeded(dir: File) {
    val parentDir = dir.getParentFile
    if (!parentDir.exists()) {
      logger.debug(s"Directory doesn't exists:${parentDir.getAbsolutePath}")
      createParentDirectoryIfNeeded(parentDir)
      if (!parentDir.mkdir()) {
        logger.error(s"Cannot create dir:${parentDir.getAbsolutePath}")
      } else {
        logger.debug(s"Directory created:${parentDir.getAbsolutePath}")
      }
    } else {
      logger.debug(s"Directory exists:${parentDir.getAbsolutePath}")
    }

  }


  private def prepareCommand(command: String): String = {
    if (isOsWindows) {
      "cmd /c " + command
    } else {
      command
    }
  }


  def isOsWindows: Boolean = {
    SystemUtils.IS_OS_WINDOWS
  }

  def pullRepoChanges(localPath: Path, repoData: RepoData, previousHead: Option[ObjectId]): LogCommand = {
    val svnRepoData = repoData.asInstanceOf[SvnRepoData]
    prepareCommand(s"echo ${svnRepoData.password}") #| Process(s"git svn rebase --quiet --username ${svnRepoData.username}", localPath.toFile) !< ProcessLogger(logger info _)
    logger.debug("SVN repo updated")
    val git = jGitFacade.gitRepo(localPath)
    previousHead match {
      case Some(sha) => git.log.addRange(sha, jGitFacade.getHeadId(localPath))
      case None => {
        logger.warn("Inconsistent repository state, cannot determine last commit in database. Rebuilding from local git log.")
        git.log
      }
    }
  }

}
