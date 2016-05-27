package com.softwaremill.codebrag.repository

import java.util.Calendar

import com.softwaremill.codebrag.repository.config.RepoData
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.slf4j.Logging
import org.eclipse.jgit.errors.MissingObjectException
import org.eclipse.jgit.lib.{Constants, ObjectId}
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}

trait Repository extends Logging with RepositorySnapshotLoader with RepositoryDeltaLoader with BranchesModel {

  def repoData: RepoData
  def repo: org.eclipse.jgit.lib.Repository
  val repoName = repoData.repoName
  val config: CodebragConfig = new CodebragConfig {
    def rootConfig = ConfigFactory.load()
  }

  def pullChanges() {
    logger.debug(s"Pulling changes for ${repoData.repoLocation}")

    if(canPullAtThisTime()) {
      try {
        pullChangesForRepo()
        logger.debug(s"Changes pulled succesfully")
      } catch {
        case e: Exception => {
          logger.error(s"Cannot pull changes for repo ${repoData.repoLocation} because of: ${e.getMessage}")
          throw e
        }
      }
    } else {
      logger.debug("Current time configuration doesn't allow to pull changes.")
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

  protected def canPullAtThisTime(): Boolean = {
    if(config.pullSleepPeriodEnabled) {
      val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
      currentHour >= config.pullSleepPeriodEnd && currentHour < config.pullSleepPeriodStart
    } else {
      true
    }
  }
}

object Repository {

  def buildUsing(data: RepoData) = {
    data.repoType match {
      case "git" => new GitRepository(data)
      case "git-svn" => new GitSvnRepository(data)
    }
  }

}