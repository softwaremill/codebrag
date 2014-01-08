package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord}
import com.softwaremill.codebrag.domain.{UserLike, CommitFileInfo, CommitInfo}
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{BsonRecordListField, MongoListField, DateField, ObjectIdPk}
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._
import com.typesafe.scalalogging.slf4j.Logging


class MongoCommitInfoDAO extends CommitInfoDAO with Logging {

  import CommitInfoImplicits._

  override def storeCommit(commit: CommitInfo) {
    commit.save
  }

  override def findBySha(sha: String): Option[CommitInfo] = {
    CommitInfoRecord where (_.sha eqs sha) get()
  }

  override def findByCommitId(commitId: ObjectId): Option[CommitInfo] = {
    CommitInfoRecord where (_.id eqs commitId) get()
  }

  override def findLastSha(): Option[String] = {
    CommitInfoRecord orderDesc(_.committerDate) andDesc(_.authorDate) get() map(_.sha.get)
  }

  override def findLastCommitsNotAuthoredByUser[T](user: T, count: Int)(implicit userLike: UserLike[T]): List[CommitInfo] = {
    CommitInfoRecord where (_.authorName neqs userLike.userFullName(user)) and (_.authorEmail neqs userLike.userEmail(user)) orderDesc(_.committerDate) andDesc(_.authorDate) limit count fetch()
  }

  override def findLastCommitsAuthoredByUser[T](user: T, count: Int)(implicit userLike: UserLike[T]): List[CommitInfo] = {
    val commitsByUserQuery = CommitInfoRecord or(_.where(_.authorEmail eqs userLike.userEmail(user)), (_.where(_.authorName eqs userLike.userFullName(user)))) orderDesc (_.committerDate) andDesc (_.authorDate)
    commitsByUserQuery limit count fetch()
  }

  override def findLastCommitsAuthoredByUserSince[T](user: T, date: DateTime)(implicit userLike: UserLike[T]): List[CommitInfo] = {
    CommitInfoRecord or(_.where(_.authorEmail eqs userLike.userEmail(user)), (_.where(_.authorName eqs userLike.userFullName(user)))) and (_.authorDate onOrAfter(date)) fetch()
  }

  override def findAllSha(): Set[String] = {
    CommitInfoRecord.select(_.sha).fetch().toSet
  }

  override def hasCommits: Boolean = {
    CommitInfoRecord.count() > 0
  }

  private object CommitInfoImplicits {

    implicit def toCommitInfo(record: CommitInfoRecord): CommitInfo = {
      CommitInfo(record.id.get, record.sha.get, record.message.get, record.authorName.get, record.authorEmail.get,
        record.committerName.get, record.committerEmail.get, new DateTime(record.authorDate.get),
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
        .authorEmail(commit.authorEmail)
        .committerName(commit.committerName)
        .committerEmail(commit.committerEmail)
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

  object authorEmail extends LongStringField(this)

  object committerName extends LongStringField(this)

  object committerEmail extends LongStringField(this)

  object authorDate extends DateField(this)

  object committerDate extends DateField(this)

  object parents extends MongoListField[CommitInfoRecord, String](this)

  object files extends BsonRecordListField(this, CommitFileInfoRecord)

}

object CommitInfoRecord extends CommitInfoRecord with MongoMetaRecord[CommitInfoRecord] {
  override def collectionName = "commit_infos"

  def ensureIndexes() {
    this.ensureIndex(keys = (committerDate.name -> 1) ~ (authorDate.name -> 1))
  }
}

class CommitFileInfoRecord extends BsonRecord[CommitFileInfoRecord] {
  def meta = CommitFileInfoRecord

  object filename extends LongStringField(this)

  object status extends LongStringField(this)

  object patch extends LongStringField(this)

}

object CommitFileInfoRecord extends CommitFileInfoRecord with BsonMetaRecord[CommitFileInfoRecord]
