package com.softwaremill.codebrag.repository

import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk.{RevWalk, RevCommit}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import org.eclipse.jgit.errors.MissingObjectException
import com.softwaremill.codebrag.repository.config.RepoData

trait Repository extends Logging {

  def repoData: RepoData
  val repo = buildRepository
  val repoName = repoData.repoName

  def pullChanges() {
    logger.debug(s"Pulling changes for ${repoData.repoLocation}")
    try {
      pullChangesForRepo
      logger.debug(s"Changes pulled succesfully")
    } catch {
      case e: Exception => {
        logger.error(s"Cannot pull changes for repo ${repoData.repoLocation} because of: ${e.getMessage}")
        throw e
      }
    }
  }

  def currentHead = {
    repo.resolve(Constants.HEAD)
  }

  def getCommits(lastKnownCommitSHA: Option[String] = None): List[RevCommit] = {
    val walker = new RevWalk(repo)
    setCommitsRange(walker, lastKnownCommitSHA)
    val commits =getCommitsAsList(walker)
    walker.dispose()
    logger.debug(s"Got ${commits.size} commit(s)")
    commits
  }

  protected def pullChangesForRepo

  private def buildRepository = {
    try {
      new FileRepositoryBuilder().setGitDir(new File(repoData.repoLocation + File.separator + ".git")).setMustExist(true).build()
    } catch {
      case e: Exception => throw new RuntimeException(s"Cannot build valid git repository object from ${repoData.repoLocation}", e)
    }
  }

  private def getCommitsAsList(walker: RevWalk) = {
    import scala.collection.JavaConversions._
    walker.iterator().toList
  }

  private def setCommitsRange(walker: RevWalk, lastKnownCommitSHA: Option[String]) {
    walker.markStart(walker.parseCommit(currentHead))
    lastKnownCommitSHA.foreach { sha =>
      try {
        val lastKnownCommit = repo.resolve(sha)
        walker.markUninteresting(walker.parseCommit(lastKnownCommit))
      } catch {
        case e: MissingObjectException => throw new RuntimeException(s"Cannot find commit with ID $sha", e)
      }
    }
  }

}

