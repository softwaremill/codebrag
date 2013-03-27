package com.softwaremill.codebrag.service.github

import org.eclipse.egit.github.core.RepositoryCommit
import com.softwaremill.codebrag.domain.CommitInfo
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
      List.empty
    )
  }

}
