package com.softwaremill.codebrag.migration

import com.softwaremill.codebrag.domain.CommitInfo
import com.softwaremill.codebrag.repository.Repository
import com.softwaremill.codebrag.repository.config.RepoDataDiscovery
import com.softwaremill.codebrag.service.config.MultiRepoConfig
import com.softwaremill.scalaval.Validation._
import org.joda.time.{DateTimeZone, DateTime}

object FixZombieCommits extends App with MigrationConfig with MultiRepoConfig {

  import daos._

  logger.debug("Fixing zombie commits")
  fixZombieCommits()
  logger.debug("Fixing zombie commits - Done")

  private def fixZombieCommits() {
    for {
      repo <- RepoDataDiscovery.discoverRepoDataFromConfig(this).map(Repository.buildUsing)
      commitsForBranch <- repo.loadCommitsSince(Map(), Int.MaxValue).commits
      repoCommit <- commitsForBranch.commits
    } {
      commitInfoDao.findBySha(repo.repoName, repoCommit.sha) match {
        case Some(dbCommit) => fixCommitDates(repoCommit, dbCommit)
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
      result.errors.map(_._2.mkString).foreach { logger.debug(_) }
      logger.debug("Updating commit {}", fixedCommitInfo.id)
      commitInfoDao.updateCommitAndAuthorDates(fixedCommitInfo)
    }
  }
}
