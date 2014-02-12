package com.softwaremill.codebrag.dao.commitinfo

import com.softwaremill.codebrag.domain.{UserLike, CommitInfo}
import org.bson.types.ObjectId
import org.joda.time.DateTime

trait CommitInfoDAO {

  def hasCommits: Boolean

  def storeCommit(commit: CommitInfo)

  def findBySha(sha: String): Option[CommitInfo]

  def findByCommitId(commitId: ObjectId): Option[CommitInfo]

  def findAllSha(): Set[String]

  def findLastSha(): Option[String]

  def findLastCommitsNotAuthoredByUser[T](user: T, count: Int)(implicit userLike: UserLike[T]): List[CommitInfo]

  def findLastCommitsAuthoredByUser[T](user: T, count: Int)(implicit userLike: UserLike[T]): List[CommitInfo]

  def findLastCommitsAuthoredByUserSince[T](user: T, date: DateTime)(implicit userLike: UserLike[T]): List[CommitInfo]

  def findPartialCommitInfo(ids: List[ObjectId]): List[PartialCommitInfo]

}