package com.softwaremill.codebrag.service.github.jgit

import org.eclipse.jgit.revwalk.RevCommit
import com.softwaremill.codebrag.domain.{CommitFileInfo, CommitInfo}
import org.gitective.core.filter.commit.CommitDiffFilter
import java.util
import org.eclipse.jgit.diff.{DiffFormatter, DiffEntry}
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import java.io.ByteArrayOutputStream
import org.eclipse.jgit.errors.StopWalkException
import org.gitective.core.CommitFinder
import org.eclipse.jgit.lib.Repository
import scala.collection.JavaConversions._
import org.joda.time.DateTime
import com.typesafe.scalalogging.slf4j.Logging

class JgitLogConverter extends Logging {

  def toCommitInfos(jGitCommits: List[RevCommit], repository: Repository): List[CommitInfo] = {

    for (jGitCommit <- jGitCommits) yield {

      var commitInfo: Option[CommitInfo] = None
      val objectId = jGitCommit.toObjectId
      val filter = new CommitDiffFilter() {

        def logIfContainsNotParseableDiff(commit: RevCommit, diffs: util.Collection[DiffEntry]) {
          if (diffs.exists(_.getOldId == null)) {
             logger.warn(s"Diff impossible to parse in commit ${commit.getId.name}")
          }
        }

        override def include(commit: RevCommit, diffs: util.Collection[DiffEntry]): Boolean = {
          if (commit.toObjectId.equals(objectId)) {
            try {
              logIfContainsNotParseableDiff(commit, diffs)
              val files = diffs.toList.map(toCommitFileInfo(_, repository))
              commitInfo = Some(buildCommitInfo(commit, files))
            }
            catch {
              case exception: Exception => throw new IllegalStateException(s"Exception while parsing commit ${objectId.getName}", exception)
            }
            throw StopWalkException.INSTANCE
          }
          true
        }
      }
      new CommitFinder(repository).setFilter(filter).find()
      commitInfo.getOrElse(throw new IllegalStateException(s"Commit not converted! $objectId"))
    }
  }


  def buildCommitInfo(jGitCommit: RevCommit, files: List[CommitFileInfo]): CommitInfo = {
    CommitInfo(
      sha = jGitCommit.toObjectId.name(),
      message = jGitCommit.getFullMessage,
      authorName = jGitCommit.getAuthorIdent.getName,
      committerName = jGitCommit.getCommitterIdent.getName,
      authorDate = new DateTime(jGitCommit.getAuthorIdent.getWhen),
      commitDate = new DateTime(jGitCommit.getCommitTime * 1000l),
      jGitCommit.getParents.map(_.toObjectId.name()).toList,
      files)
  }

  private def toCommitFileInfo(diff: DiffEntry, repository: Repository): CommitFileInfo = {
    val filename = diff.getChangeType match {
      case ChangeType.ADD => diff.getNewPath
      case _ => diff.getOldPath
    }

    val status = changeTypeToStatus(diff.getChangeType)
    val patchString = if (diff.getOldId == null) {
      "+ patch unavailable due to JGit bug 407743"
    }
    else {
      val baos = new ByteArrayOutputStream()
      val formatter = new DiffFormatter(baos)
      try {
        formatter.setRepository(repository)
        formatter.format(diff)
        baos.toString()
      }
      finally {
        formatter.release()
      }
    }
    CommitFileInfo(filename, status, patchString)
  }

  private def changeTypeToStatus(change: ChangeType): String = {
    change match {
      case ChangeType.ADD => "added"
      case ChangeType.DELETE => "deleted"
      case ChangeType.MODIFY => "modified"
      case ChangeType.COPY => "copied"
      case ChangeType.RENAME => "renamed"
      case _ => "unknown"
    }
  }

}
