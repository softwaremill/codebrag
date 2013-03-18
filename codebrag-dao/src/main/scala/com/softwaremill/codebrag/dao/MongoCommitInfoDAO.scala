package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.CommitInfo
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import com.foursquare.rogue.LiftRogue._

class MongoCommitInfoDAO extends CommitInfoDAO {

  import CommitInfoImplicits._

  def storeCommit(commit: CommitInfo) {
    commit.save
  }

  def storeCommits(commits: Seq[CommitInfo]) {
    CommitInfoRecord.insertAll(commits.toList)
  }

  def findBySha(sha: String): Option[CommitInfo] = {
    CommitInfoRecord where (_.sha eqs sha) get()
  }

  private object CommitInfoImplicits {

    implicit def toCommitInfo(record: CommitInfoRecord): CommitInfo = {
      CommitInfo(record.sha.get)
    }

    implicit def toCommitInfo(record: Option[CommitInfoRecord]): Option[CommitInfo] = {
      record.map(toCommitInfo(_))
    }

    implicit def toCommitInfoRecord(commit: CommitInfo): CommitInfoRecord = {
      CommitInfoRecord.createRecord.sha(commit.sha)
    }

    implicit def toCommitInfoRecordList(commits: List[CommitInfo]): List[CommitInfoRecord] = {
      commits.map(toCommitInfoRecord(_))
    }
  }

}

private class CommitInfoRecord extends MongoRecord[CommitInfoRecord] with ObjectIdPk[CommitInfoRecord] {
  def meta = CommitInfoRecord

  object sha extends LongStringField(this)

}

private object CommitInfoRecord extends CommitInfoRecord with MongoMetaRecord[CommitInfoRecord] {
  override def collectionName: String = "commitInfos"
}
