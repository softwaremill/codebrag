package com.softwaremill.codebrag.service.commits.jgit

import org.eclipse.jgit.revwalk.RevCommit
import com.softwaremill.codebrag.domain.{CommitFileInfo, CommitInfo}
import org.eclipse.jgit.lib.Repository
import org.joda.time.DateTime
import com.typesafe.scalalogging.slf4j.Logging

class JgitLogConverter extends Logging with JgitDiffExtractor {

  def toCommitInfos(jGitCommits: List[RevCommit], repository: Repository): List[CommitInfo] = {

    for (jGitCommit <- jGitCommits) yield {
      val files = extractDiffsFromCommit(jGitCommit, repository)
      buildCommitInfo(jGitCommit, files)
    }
  }

  def buildCommitInfo(jGitCommit: RevCommit, files: List[CommitFileInfo]): CommitInfo = {
    CommitInfo(
      sha = jGitCommit.toObjectId.name(),
      message = encodingSafeCommitMessage(jGitCommit),
      authorName = jGitCommit.getAuthorIdent.getName,
      authorEmail = jGitCommit.getAuthorIdent.getEmailAddress,
      committerName = jGitCommit.getCommitterIdent.getName,
      committerEmail = jGitCommit.getCommitterIdent.getEmailAddress,
      authorDate = new DateTime(jGitCommit.getAuthorIdent.getWhen),
      commitDate = new DateTime(jGitCommit.getCommitTime * 1000l),
      jGitCommit.getParents.map(_.toObjectId.name()).toList,
      files)
  }

  def encodingSafeCommitMessage(jGitCommit: RevCommit): String = {
    try {
      val msg = jGitCommit.getFullMessage
      msg
    } catch {
      case e: Exception => logger.error(s"Cannot read message for commit ${jGitCommit.toObjectId.name()}", e)
        "[unknown commit message]"
    }
  }

}
