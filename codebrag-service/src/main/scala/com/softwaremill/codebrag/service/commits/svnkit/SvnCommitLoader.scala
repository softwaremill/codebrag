package com.softwaremill.codebrag.service.commits.svnkit

import com.softwaremill.codebrag.service.commits.{SvnRepoData, RepoData, CommitsLoader}
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.CommitInfo
import com.softwaremill.codebrag.service.commits.jgit.InternalDirTree
import org.tmatesoft.svn.core.wc.{SVNRevision, SVNClientManager}
import org.tmatesoft.svn.core.{SVNLogEntry, ISVNLogEntryHandler, SVNDepth, SVNURL}
import java.io.File
import java.nio.file.Files
import com.softwaremill.codebrag.dao.RepositoryHeadStore
import org.eclipse.jgit.lib.ObjectId
import org.joda.time.DateTime

class SvnCommitLoader(internalDirTree: InternalDirTree, svnFacade: SvnFacade,
                      repoHeadDao: RepositoryHeadStore) extends CommitsLoader with Logging {


  def loadMissingCommits(repoData: RepoData): List[CommitInfo] = {
    if (repoData.credentialsValid) {
      loadCommits(repoData)
    } else {
      dontLoadCommits
    }
  }

  def loadCommits(repoData: RepoData): List[CommitInfo] = {
    val start = new DateTime();

    val svnRepoData = repoData.asInstanceOf[SvnRepoData]

    if (!internalDirTree.containsRepo(repoData))
      Files.createDirectories(internalDirTree.getPath(repoData))

    val checkoutDir = internalDirTree.getPath(repoData).toFile

    val headRev = svnFacade.update(checkoutDir, repoData.remoteUri, svnRepoData)

    val commits = fetchPreviousHead(repoData) match {
      case Some(head) => svnFacade.log(checkoutDir, head, svnRepoData)
      case None => {
        logger.warn("Incosistent repository state, cannot determine last commit in database. Rebuilding from scratch.")
        svnFacade.log(checkoutDir, "0", svnRepoData)
      }
    }

    repoHeadDao.update(repoData.remoteUri, lastProcessedCommitOrHeadWhenFinished(commits, headRev.toString))

    logger.debug("Finished commit analysis in " + (new DateTime().getMillis - start.getMillis) + " milliseconds")

    commits
  }

  private def lastProcessedCommitOrHeadWhenFinished(commits: List[CommitInfo], head: String) = {
    if (SvnFacade.MAX_COMMITS.equals(commits.size)) commits.reverse.head.sha else head
  }

  private def dontLoadCommits = {
    logger.warn("Invalid repository data, can't import commits")
    List.empty
  }

  private def fetchPreviousHead(repoData: RepoData): Option[String] = {
    repoHeadDao.get(repoData.remoteUri)
  }
}
