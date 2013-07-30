package com.softwaremill.codebrag.service.commits.jgit

import java.nio.file.Path
import com.softwaremill.codebrag.service.commits.{SvnRepoData, RepoData}
import org.eclipse.jgit.lib.ObjectId
import scala.sys.process._
import com.softwaremill.codebrag.dao.RepositoryHeadStore
import org.eclipse.jgit.api.{LogCommand}
import com.typesafe.scalalogging.slf4j.Logging

class GitSvnRepoUpdater(jGitFacade: JgitFacade, repoHeadDao: RepositoryHeadStore) extends RepoUpdater with Logging {
  def cloneFreshRepo(localPath: Path, repoData: RepoData): LogCommand = {
    val svnRepoData = repoData.asInstanceOf[SvnRepoData]

    s"echo ${svnRepoData.password}" #| s"git svn clone ${svnRepoData.remoteUri} --quiet --username ${svnRepoData.username} ${localPath.toString}" !< ProcessLogger(logger info _)

    logger info "SVN repo checked out"

    val headAfterPull = jGitFacade.getHeadId(localPath)
    repoHeadDao.update(repoData.remoteUri, ObjectId.toString(headAfterPull))

    jGitFacade.gitRepo(localPath).log()
  }

  def pullRepoChanges(localPath: Path, repoData: RepoData, previousHead: Option[ObjectId]): LogCommand = {
    val svnRepoData = repoData.asInstanceOf[SvnRepoData]

    s"echo ${svnRepoData.password}" #| Process(s"git svn rebase --quiet --username ${svnRepoData.username}", localPath.toFile) !< ProcessLogger(logger info _)

    logger info "SVN repo updated"

    val headAfterPull = jGitFacade.getHeadId(localPath)
    repoHeadDao.update(repoData.remoteUri, ObjectId.toString(headAfterPull))

    val git =  jGitFacade.gitRepo(localPath)

    previousHead match {
      case Some(sha) => git.log.addRange(sha, headAfterPull)
      case None => {
        logger.warn("Incosistent repository state, cannot determine last commit in database. Rebuilding from local git log.")
        git.log
      }
    }
  }

}
