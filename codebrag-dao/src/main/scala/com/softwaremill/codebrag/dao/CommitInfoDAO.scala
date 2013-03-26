package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.CommitInfo

trait CommitInfoDAO {

  def storeCommit(commit: CommitInfo)

  def storeCommits(commits: Seq[CommitInfo])

  def findBySha(sha: String): Option[CommitInfo]

  def findAll(): List[CommitInfo]
}