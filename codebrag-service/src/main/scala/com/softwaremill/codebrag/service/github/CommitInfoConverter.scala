package com.softwaremill.codebrag.service.github

import org.eclipse.egit.github.core.{CommitFile, RepositoryCommit}
import com.softwaremill.codebrag.domain.{CommitFileInfo, CommitInfo}
import scala.collection.JavaConversions._
import org.joda.time.DateTime
import com.softwaremill.codebrag.common.IdGenerator

trait CommitInfoConverter[T] {
  def convertToCommitInfo(commit: T): CommitInfo
}

class GitHubCommitInfoConverter(implicit idGenerator: IdGenerator) extends CommitInfoConverter[RepositoryCommit] {
  override def convertToCommitInfo(commit: RepositoryCommit): CommitInfo = {
    val rawCommit = commit.getCommit
    CommitInfo(
      idGenerator.generateRandom(),
      commit.getSha,
      rawCommit.getMessage,
      rawCommit.getAuthor.getName,
      rawCommit.getCommitter.getName,
      new DateTime(rawCommit.getAuthor.getDate),
      commit.getParents.map(_.getSha).toList,
      List.empty,
      files(commit)
    )
  }

  def files(commit: RepositoryCommit): List[CommitFileInfo] = {
    Option(commit.getFiles) match {
      case None => List()
      case Some(list) => list.map(convertToCommitFileInfo(_)).toList
    }
  }

  def convertToCommitFileInfo(file: CommitFile): CommitFileInfo = {
    CommitFileInfo(file.getFilename, file.getPatch)
  }

}
