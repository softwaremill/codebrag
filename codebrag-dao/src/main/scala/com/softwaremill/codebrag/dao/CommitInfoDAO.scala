package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.CommitInfo
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk

trait CommitInfoDAO {
  def storeCommit(commit: CommitInfo)

  def storeCommits(commits: Seq[CommitInfo])
}


class CommitRecord extends MongoRecord[CommitRecord] with ObjectIdPk[CommitRecord] {
  def meta = CommitRecord
}

object CommitRecord extends CommitRecord with MongoMetaRecord[CommitRecord]