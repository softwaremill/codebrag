package com.softwaremill.codebrag.service.commits.jgit

import org.eclipse.jgit.revwalk.RevCommit
import com.softwaremill.codebrag.domain.{CommitFileInfo, CommitInfo}
import org.eclipse.jgit.lib.{PersonIdent, Repository}
import org.joda.time.DateTime
import com.typesafe.scalalogging.slf4j.Logging

class JgitLogConverter extends Logging with JgitDiffExtractor {

  def toCommitInfos(jGitCommits: List[RevCommit], repository: Repository): List[CommitInfo] = {
    val commitsOptions = jGitCommits.map(buildCommitInfoSafely(_, repository))
    commitsOptions.filter(_.isDefined).map(_.get)
  }

  def buildCommitInfoSafely(commit: RevCommit, repository: Repository): Option[CommitInfo] = {
    try {
      Some(buildCommitInfo(commit, extractDiffsFromCommit(commit, repository)))
    } catch {
      case e: Exception => {
        logger.error(s"Cannot import commit with ID ${commit.toObjectId.name()}. Skipping this one")
        logger.debug("Exception details", e)
        None
      }
    }
  }

  private def buildCommitInfo(jGitCommit: RevCommit, files: List[CommitFileInfo]): CommitInfo = {
    CommitInfo(
      sha = jGitCommit.toObjectId.name(),
      message = jGitCommit.getFullMessage,
      authorName = jGitCommit.getAuthorIdent.getName,
      authorEmail = jGitCommit.getAuthorIdent.getEmailAddress,
      committerName = jGitCommit.getCommitterIdent.getName,
      committerEmail = jGitCommit.getCommitterIdent.getEmailAddress,
      authorDate = new DateTime(jGitCommit.getAuthorIdent.getWhen),
      commitDate = new DateTime(jGitCommit.getCommitTime * 1000l),
      jGitCommit.getParents.map(_.toObjectId.name()).toList,
      files)
  }

}
