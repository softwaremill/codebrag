package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{UserLike, CommitInfo}
import org.bson.types.ObjectId

trait CommitInfoDAO {

  def hasCommits: Boolean

  def storeCommit(commit: CommitInfo)

  def findBySha(sha: String): Option[CommitInfo]

  def findByCommitId(commitId: ObjectId): Option[CommitInfo]

  def findAllSha(): Set[String]

  def findLastSha(): Option[String]

  def findNewestCommitsNotAuthoredByUser[T](user: T, count: Int)(implicit userLike: UserLike[T]): List[CommitInfo]

  def findLastCommitAuthoredByUser[T](user: T)(implicit userLike: UserLike[T]): Option[CommitInfo]

}