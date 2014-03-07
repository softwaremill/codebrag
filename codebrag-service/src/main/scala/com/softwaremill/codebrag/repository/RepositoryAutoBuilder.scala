package com.softwaremill.codebrag.repository

import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.repository.config.RepoData
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

trait RepositoryAutoBuilder extends Logging {

  def repoData: RepoData

  val repo = {
    logger.debug(s"Building repository object from ${repoData.repoLocation}")
    try {
      new FileRepositoryBuilder().setGitDir(new File(repoData.repoLocation + File.separator + ".git")).setMustExist(true).build()
    } catch {
      case e: Exception => throw new RuntimeException(s"Cannot build valid git repository object from ${repoData.repoLocation}", e)
    }
  }

}