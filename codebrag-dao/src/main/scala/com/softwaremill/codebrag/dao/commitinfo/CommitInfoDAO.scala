package com.softwaremill.codebrag.dao.commitinfo

import com.softwaremill.codebrag.domain.{PartialCommitInfo, UserLike, CommitInfo}
import org.bson.types.ObjectId
import org.joda.time.DateTime

trait CommitInfoDAO {

  def hasCommits: Boolean

  def storeCommit(commit: CommitInfo): CommitInfo

  def findBySha(repoName: String, sha: String): Option[CommitInfo]

  def findByShaList(repoName: String, shaList: List[String]): List[PartialCommitInfo]

  def findByCommitId(commitId: ObjectId): Option[CommitInfo] // TODO to remove?

  def findAllSha(repoName: String): Set[String]

  def findAllIds(): List[ObjectId]  // TODO to remove?

  def findLastSha(repoName: String): Option[String]

  def findLastCommitsNotAuthoredByUser[T](repoName: String, user: T, count: Int)(implicit userLike: UserLike[T]): List[CommitInfo]

  def findLastCommitsAuthoredByUser[T](repoName: String, user: T, count: Int)(implicit userLike: UserLike[T]): List[CommitInfo]

  def findLastCommitsAuthoredByUserSince[T](repoName: String, user: T, date: DateTime)(implicit userLike: UserLike[T]): List[CommitInfo]

  def findPartialCommitInfo(ids: List[ObjectId]): List[PartialCommitInfo] // TODO: to remove?


  // temporary methods to preserve old version compatibility for now
  def findBySha(sha: String): Option[CommitInfo]
  def findByShaList(shaList: List[String]): List[PartialCommitInfo]
  def findAllSha(): Set[String]
  def findLastSha(): Option[String]
  def findLastCommitsNotAuthoredByUser[T](user: T, count: Int)(implicit userLike: UserLike[T]): List[CommitInfo]
  def findLastCommitsAuthoredByUser[T](user: T, count: Int)(implicit userLike: UserLike[T]): List[CommitInfo]
  def findLastCommitsAuthoredByUserSince[T](user: T, date: DateTime)(implicit userLike: UserLike[T]): List[CommitInfo]
}