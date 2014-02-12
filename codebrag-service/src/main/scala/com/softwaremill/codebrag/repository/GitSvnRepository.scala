package com.softwaremill.codebrag.repository

import org.apache.commons.lang3.SystemUtils
import scala.sys.process._
import java.nio.file.Paths
import com.softwaremill.codebrag.repository.config.GitSvnRepoConfig

class GitSvnRepository(val repoConfig: GitSvnRepoConfig) extends Repository {

  def pullChanges {
    try {
      runPullCommand
      logger.debug(s"Changes pulled succesfully")
    } catch {
      case e: Exception => throw new RuntimeException(s"Cannot pull changes for repo: ${repoConfig.repoLocation}", e)
    }
  }

  private def runPullCommand = {
    val repoPath = Paths.get(repoConfig.repoLocation)
    repoConfig.credentials match {
      case Some(c) => {
        callOsCommand(s"echo ${c.pass}") #| Process(s"git svn rebase --quiet --username ${c.user}", repoPath.toFile) !< ProcessLogger(logger info _)
      }
      case None => {
        Process(s"git svn rebase --quiet", repoPath.toFile) !< ProcessLogger(logger info _)
      }
    }
  }

  private def callOsCommand(command: String): String = {
    if (SystemUtils.IS_OS_WINDOWS) {
      "cmd /c " + command
    } else {
      command
    }
  }

}
