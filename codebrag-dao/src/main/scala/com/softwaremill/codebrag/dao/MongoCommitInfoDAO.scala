package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord}
import com.softwaremill.codebrag.domain.{CommitFileInfo, CommitInfo}
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{BsonRecordListField, MongoListField, DateField, ObjectIdPk}
import com.foursquare.rogue.LiftRogue._
import org.joda.time.{Interval, DateTime}
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._


class MongoCommitInfoDAO extends CommitInfoDAO {

  import CommitInfoImplicits._

  override def storeCommit(commit: CommitInfo) {
    commit.save
  }

  override def findBySha(sha: String): Option[CommitInfo] = {
    CommitInfoRecord where (_.sha eqs sha) get()
  }

  override def findForTimeRange(interval: Interval): List[CommitInfo] = {
    CommitInfoRecord.where(_.committerDate between(interval.getStart, interval.getEnd)).fetch()
  }

  override def findByCommitId(commitId: ObjectId): Option[CommitInfo] = {
    CommitInfoRecord where (_.id eqs commitId) get()
  }

  override def findLastSha(): Option[String] = {
    CommitInfoRecord.orderDesc(_.committerDate).get().map(_.sha.get)
  }

  override def findAllSha(): Set[String] = {
    CommitInfoRecord.select(_.sha).fetch().toSet
  }

  override def hasCommits: Boolean = {
    CommitInfoRecord.count() > 0
  }

  private object CommitInfoImplicits {

    implicit def toCommitInfo(record: CommitInfoRecord): CommitInfo = {
      CommitInfo(record.id.get, record.sha.get, record.message.get, record.authorName.get, record.committerName.get, new DateTime(record.authorDate.get),
        new DateTime(record.committerDate.get), record.parents.get, record.files.get.toList)
    }


    implicit def toCommitInfo(record: Option[CommitInfoRecord]): Option[CommitInfo] = {
      record.map(toCommitInfo(_))
    }

    implicit def toCommitInfoRecord(commit: CommitInfo): CommitInfoRecord = {
      CommitInfoRecord.createRecord
        .id(commit.id)
        .sha(commit.sha)
        .message(commit.message)
        .authorName(commit.authorName)
        .committerName(commit.committerName)
        .authorDate(commit.authorDate.toDate)
        .committerDate(commit.commitDate.toDate)
        .parents(commit.parents)
        .files(commit.files)
    }

    implicit def toCommitInfoRecordList(commits: List[CommitInfo]): List[CommitInfoRecord] = {
      commits.map(toCommitInfoRecord(_))
    }

    implicit def toCommitInfoList(commits: List[CommitInfoRecord]): List[CommitInfo] = {
      commits.map(toCommitInfo(_))
    }

    implicit def toCommitFileInfoList(files: List[CommitFileInfoRecord]): List[CommitFileInfo] = {
      files.map(file => CommitFileInfo(file.filename.get, file.status.get, file.patch.get))
    }

    implicit def commitFilesToCommitFilesRecords(files: List[CommitFileInfo]):List[CommitFileInfoRecord]= {
      files.map(file => CommitFileInfoRecord.createRecord.filename(file.filename).status(file.status).patch(file.patch))
    }
  }
}

class CommitInfoRecord extends MongoRecord[CommitInfoRecord] with ObjectIdPk[CommitInfoRecord] {
  def meta = CommitInfoRecord

  object sha extends LongStringField(this)

  object message extends LongStringField(this)

  object authorName extends LongStringField(this)

  object committerName extends LongStringField(this)

  object authorDate extends DateField(this)

  object committerDate extends DateField(this)

  object parents extends MongoListField[CommitInfoRecord, String](this)

  object files extends BsonRecordListField(this, CommitFileInfoRecord)

}

object CommitInfoRecord extends CommitInfoRecord with MongoMetaRecord[CommitInfoRecord] {
  override def collectionName = "commit_infos"

  def ensureIndexes() {
    this.ensureIndex(committerDate.name -> 1, authorDate.name -> 1)
  }
}

class CommitFileInfoRecord extends BsonRecord[CommitFileInfoRecord] {
  def meta = CommitFileInfoRecord

  object filename extends LongStringField(this)

  object status extends LongStringField(this)

  object patch extends LongStringField(this)

}

object CommitFileInfoRecord extends CommitFileInfoRecord with BsonMetaRecord[CommitFileInfoRecord]
