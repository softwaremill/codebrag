package com.softwaremill.codebrag.repository

import com.softwaremill.codebrag.repository.config.RepoData
import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.jgit.errors.MissingObjectException
import org.eclipse.jgit.lib.{Constants, ObjectId}
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}

trait Repository extends Logging with RepositorySnapshotLoader with RepositoryDeltaLoader with BranchesModel {

  def repoData: RepoData
  def repo: org.eclipse.jgit.lib.Repository
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

  protected def pullChangesForRepo()

  protected def branchNameToSHA(objId: ObjectId) = ObjectId.toString(objId)
}

object Repository {

  def buildUsing(data: RepoData) = {
    data.repoType match {
      case "git" => new GitRepository(data)
      case "git-svn" => new GitSvnRepository(data)
    }
  }

}