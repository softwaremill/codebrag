package com.softwaremill.codebrag.migration

import com.softwaremill.codebrag.common.RealTimeClock
import com.softwaremill.codebrag.dao.sql.SQLDatabase
import com.softwaremill.codebrag.dao.{DaoConfig, Daos}
import com.softwaremill.codebrag.domain.{CommitInfo, UserWatchedBranch}
import com.softwaremill.codebrag.repository.Repository
import com.softwaremill.codebrag.repository.config.RepoDataDiscovery
import com.softwaremill.codebrag.service.config.{CommitCacheConfig, MultiRepoConfig}
import com.softwaremill.scalaval.Validation.{rule, validate}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.Logging
import org.bson.types.ObjectId
import org.joda.time.{DateTime, DateTimeZone}

trait MigrationConfig extends DaoConfig with MultiRepoConfig with CommitCacheConfig {

  def rootConfig = ConfigFactory.load()

  val daos = new Daos {
    val sqlDatabase = SQLDatabase.createEmbedded(MigrationConfig.this)
    val clock = RealTimeClock
  }
}

object MigrateV2_2ToV2_3 extends App with Logging with MigrationConfig {

  import daos._

  sqlDatabase.db.withDynSession {
    logger.debug("Updating database schema")
    sqlDatabase.updateSchema()
    logger.debug(s"Updating watched branches")
    userRepoDetailsDao.findAll().map { userRepo =>
      val branchName = if (userRepo.branchName.isEmpty) "master" else userRepo.branchName
      UserWatchedBranch(new ObjectId(), userRepo.userId, userRepo.repoName, branchName)
    } foreach userObservedBranchesDao.save
    logger.debug(s"Updating watched branches - Done")
  }

  logger.debug("Fixing zombie commits")
  fixZombieCommits()
  logger.debug("Fixing zombie commits - Done")

  private def fixZombieCommits() {
    val repos = RepoDataDiscovery.discoverRepoDataFromConfig(this).map(Repository.buildUsing)

    repos.foreach { repo =>
      val repoCommits = repo.loadCommitsSince(Map(), Int.MaxValue)

      repoCommits.commits.foreach { commitsForBranch =>
        commitsForBranch.commits.foreach { repoCommit =>
          commitInfoDao.findBySha(repo.repoData.repoName, repoCommit.sha) match {
            case Some(dbCommit) => fixCommitDates(repoCommit, dbCommit)
          }
        }
      }
    }
  }

  private def fixCommitDates(repoCommit: CommitInfo, dbCommit: CommitInfo) {
    def dateCheck(label: String, date: CommitInfo => DateTime) = {
      val result = date(dbCommit).isEqual(date(repoCommit))
      val message = s"$label date is invalid (current: ${date(dbCommit)}, actual: ${date(repoCommit).withZone(DateTimeZone.UTC)})"
      (result, message)
    }

    val commitDateValid = rule("commitDate") { dateCheck("Commit", { _.commitDate }) }
    val authorDateValid = rule("authorDate") { dateCheck("Author", { _.authorDate }) }
    val result = validate(commitDateValid, authorDateValid)

    if (result.errors.nonEmpty) {
      val fixedCommitInfo = dbCommit.copy(commitDate = repoCommit.commitDate, authorDate = repoCommit.authorDate)
      logger.debug("Validation failed for commit {} (SHA: {})", fixedCommitInfo.id, fixedCommitInfo.sha)
      result.errors.foreach { error => logger.debug(error._2.mkString) }
      logger.debug("Updating commit {}", fixedCommitInfo.id)
      commitInfoDao.updateCommitAndAuthorDates(fixedCommitInfo)
    }
  }
}
