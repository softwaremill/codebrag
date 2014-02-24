package com.softwaremill.codebrag.service.commits

import com.softwaremill.codebrag.dao.CommitInfoDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.{CommitInfo, UpdatedCommit, CommitsUpdatedEvent}
import com.softwaremill.codebrag.common.{Clock, EventBus}

class CommitImportService(commitsLoader: CommitsLoader, commitInfoDao: CommitInfoDAO, eventBus: EventBus)(implicit clock: Clock) extends Logging {

  def importRepoCommits(repoData: RepoData) {
    logger.debug("Start loading commits")
    val commitsLoaded = commitsLoader.loadMissingCommits(repoData)
    logger.debug(s"Commits loaded: ${commitsLoaded.size}")
    val isFirstImport = !commitInfoDao.hasCommits
    val storedCommits = storeCommits(commitsLoaded)
    if (!storedCommits.isEmpty) {
      eventBus.publish(CommitsUpdatedEvent(isFirstImport, storedCommits))
    }
    logger.debug("Commits stored. Loading finished.")
  }

  def storeCommits(commitsLoaded: List[CommitInfo]): List[UpdatedCommit] = {
    commitsLoaded.flatMap { commit =>
      try {
        commitInfoDao.storeCommit(commit)
        val basicCommitInfo = UpdatedCommit(commit.id, commit.authorName, commit.authorEmail, commit.commitDate)
        Some(basicCommitInfo)
      } catch {
        case e: Exception => {
          logger.error(s"Cannot store commit ${commit.sha}. Skipping this one", e.getMessage)
          None
        }
      }
    }
  }

}