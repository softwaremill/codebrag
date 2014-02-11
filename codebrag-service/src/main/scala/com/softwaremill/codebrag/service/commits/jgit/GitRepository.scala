package com.softwaremill.codebrag.service.commits.jgit

import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eclipse.jgit.errors.MissingObjectException

/**
 * Encapsulates all required operations on already initialized git repo
 * It works on current branch only and can pull changes and list commits
 * either all or only those to specified commit (starting from HEAD)
 * @param location location of repository on file system
 */
class GitRepository(location: String) extends Logging {

  val repo = buildRepository

  def pullChanges {
    logger.debug(s"Pulling changes for $location")
    try {
      new Git(repo).pull().call()
      logger.debug(s"Changes pulled succesfully")
    } catch {
      case e: Exception => throw new RuntimeException(s"Cannot pull changes for repo: $location", e)
    }
  }
  
  def getCommits(lastKnownCommitSHA: Option[String] = None): List[RevCommit] = {
    val walker = new RevWalk(repo)
    setCommitsRange(walker, lastKnownCommitSHA)
    val commits =getCommitsAsList(walker)
    walker.dispose()
    logger.debug(s"Got ${commits.size} commit(s)")
    commits
  }

  private def getCommitsAsList(walker: RevWalk) = {
    import scala.collection.JavaConversions._
    walker.iterator().toList
  }

  private def setCommitsRange(walker: RevWalk, lastKnownCommitSHA: Option[String]) {
    val currentHEAD = repo.resolve(Constants.HEAD)
    walker.markStart(walker.parseCommit(currentHEAD))
    lastKnownCommitSHA.foreach { sha =>
      try {
        val lastKnownCommit = repo.resolve(sha)
        walker.markUninteresting(walker.parseCommit(lastKnownCommit))
      } catch {
        case e: MissingObjectException => throw new RuntimeException(s"Cannot find commit with ID $sha", e)
      }
    }
  }

  private def buildRepository = {
    try {
      new FileRepositoryBuilder().setGitDir(new File(location + File.separator + ".git")).setMustExist(true).build()
    } catch {
      case e: Exception => throw new RuntimeException(s"Cannot build valid git repository object from $location", e)
    }
  }

}