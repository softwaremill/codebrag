package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.CommitInfo
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{MongoListField, DateField, ObjectIdPk}
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime

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

  def findAllPendingCommits(): List[CommitInfo] = {
    CommitInfoRecord.orderDesc(_.date).fetch()
  }

  private object CommitInfoImplicits {

    implicit def toCommitInfo(record: CommitInfoRecord): CommitInfo = {
      CommitInfo(record.sha.get, record.message.get, record.authorName.get, record.committerName.get, new DateTime(record.date.get), record.parents.get)
    }

    implicit def toCommitInfo(record: Option[CommitInfoRecord]): Option[CommitInfo] = {
      record.map(toCommitInfo(_))
    }

    implicit def toCommitInfoRecord(commit: CommitInfo): CommitInfoRecord = {
      CommitInfoRecord.createRecord
        .sha(commit.sha)
        .message(commit.message)
        .authorName(commit.authorName)
        .committerName(commit.committerName)
        .date(commit.date.toDate)
        .parents(commit.parents)
    }

    implicit def toCommitInfoRecordList(commits: List[CommitInfo]): List[CommitInfoRecord] = {
      commits.map(toCommitInfoRecord(_))
    }
    implicit def toCommitInfoList(commits: List[CommitInfoRecord]): List[CommitInfo] = {
      commits.map(toCommitInfo(_))
    }

  }

}

private class CommitInfoRecord extends MongoRecord[CommitInfoRecord] with ObjectIdPk[CommitInfoRecord] {
  def meta = CommitInfoRecord

  object sha extends LongStringField(this)

  object message extends LongStringField(this)

  object authorName extends LongStringField(this)

  object committerName extends LongStringField(this)

  object date extends DateField(this)

  object parents extends MongoListField[CommitInfoRecord, String](this)

}

private object CommitInfoRecord extends CommitInfoRecord with MongoMetaRecord[CommitInfoRecord] {
  override def collectionName: String = "commitInfos"
}
