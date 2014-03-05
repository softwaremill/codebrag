package com.softwaremill.codebrag.repository

import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.jgit.lib.{ObjectId, Constants}
import org.eclipse.jgit.revwalk.{RevWalk, RevCommit}
import org.eclipse.jgit.storage.file.{FileRepository, FileRepositoryBuilder}
import java.io.File
import org.eclipse.jgit.errors.MissingObjectException
import com.softwaremill.codebrag.repository.config.RepoData
import scala.collection.JavaConversions._
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand.ListMode
import com.softwaremill.codebrag.domain.{CommitsForBranch, MultibranchLoadCommitsResult}
import com.softwaremill.codebrag.service.commits.jgit.RawCommitsConverter
import org.eclipse.jgit.revwalk.filter.MaxCountRevFilter

trait Repository extends Logging with RepositorySnapshotLoader with RepositoryDeltaLoader {

  def repoData: RepoData
  val repo = buildRepository
  val repoName = repoData.repoName

  def pullChanges() {
    logger.debug(s"Pulling changes for ${repoData.repoLocation}")
    try {
      pullChangesForRepo()
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

  def getCommit(sha: String): Option[RevCommit] = {
    Option(repo.resolve(sha)).flatMap { commitId =>
      try {
        Some(new RevWalk(repo).parseCommit(commitId))
      } catch {
        case e: MissingObjectException => None
      }
    }
  }

  def getCommits(lastKnownCommitSHA: Option[String] = None): List[RevCommit] = {
    getCommitsForBranch("refs/heads/master", lastKnownCommitSHA)
  }
  

  protected def pullChangesForRepo()

  private def buildRepository = {
    try {
      new FileRepositoryBuilder().setGitDir(new File(repoData.repoLocation + File.separator + ".git")).setMustExist(true).build()
    } catch {
      case e: Exception => throw new RuntimeException(s"Cannot build valid git repository object from ${repoData.repoLocation}", e)
    }
  }

  implicit def gitObjectIdToString(objId: ObjectId) = ObjectId.toString(objId)
}