package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.CommitInfo
import org.bson.types.ObjectId
import org.joda.time.Interval

trait CommitInfoDAO {

  def hasCommits: Boolean

  def storeCommit(commit: CommitInfo)

  def findBySha(sha: String): Option[CommitInfo]

  def findByCommitId(commitId: ObjectId): Option[CommitInfo]

  def findForTimeRange(interval: Interval): List[CommitInfo]

  def findAllSha(): Set[String]

  def findLastSha(): Option[String]
}