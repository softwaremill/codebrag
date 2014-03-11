package com.softwaremill.codebrag.dao.commitinfo

import com.softwaremill.codebrag.domain.{PartialCommitInfo, UserLike, CommitInfo}
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{MongoListField, DateField, ObjectIdPk}
import com.foursquare.rogue.LiftRogue._
import org.joda.time.{DateTimeZone, DateTime}
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.dao.mongo.LongStringField


class MongoCommitInfoDAO extends CommitInfoDAO with Logging {

  import CommitInfoImplicits._

  override def storeCommit(commit: CommitInfo) = {
    val record = toCommitInfoRecord(commit)
    record.save
    toCommitInfo(record)
  }

  override def findBySha(sha: String): Option[CommitInfo] = {
    CommitInfoRecord where (_.sha eqs sha) get()
  }

  override def findByShaList(shaList: List[String]): List[PartialCommitInfo] = {
    CommitInfoRecord where (_.sha in shaList) fetch() map toPartialCommitInfo
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

  override def findAllIds(): List[ObjectId] = {
    CommitInfoRecord.select(_.id).orderAsc(_.committerDate).andAsc(_.authorDate).fetch()
  }

  override def hasCommits: Boolean = {
    CommitInfoRecord.count() > 0
  }

  override def findPartialCommitInfo(ids: List[ObjectId]) = {
    CommitInfoRecord
      .select(_.id, _.sha, _.message, _.authorName, _.authorEmail, _.authorDate)
      .where(_.id in ids)
      .orderAsc(_.committerDate)
      .andAsc(_.authorDate)
      .fetch()
      .map { commit =>
        PartialCommitInfo(commit._1, commit._2, commit._3, commit._4, commit._5, new DateTime(commit._6))
      }
  }

  private object CommitInfoImplicits {

    implicit def toCommitInfo(record: CommitInfoRecord): CommitInfo = {
      CommitInfo(record.id.get, record.sha.get, record.message.get, record.authorName.get, record.authorEmail.get,
        record.committerName.get, record.committerEmail.get, new DateTime(record.authorDate.get).withZone(DateTimeZone.UTC),
        new DateTime(record.committerDate.get).withZone(DateTimeZone.UTC), record.parents.get)
    }


    implicit def toCommitInfo(record: Option[CommitInfoRecord]): Option[CommitInfo] = {
      record.map(toCommitInfo(_))
    }

    implicit def toPartialCommitInfo(record: CommitInfoRecord): PartialCommitInfo = {
      PartialCommitInfo(toCommitInfo(record))
    }

    implicit def toCommitInfoRecord(commit: CommitInfo): CommitInfoRecord = {
      CommitInfoRecord.createRecord
        .id(new ObjectId)
        .sha(commit.sha)
        .message(commit.message)
        .authorName(commit.authorName)
        .authorEmail(commit.authorEmail)
        .committerName(commit.committerName)
        .committerEmail(commit.committerEmail)
        .authorDate(commit.authorDate.toDate)
        .committerDate(commit.commitDate.toDate)
        .parents(commit.parents)
    }

    implicit def toCommitInfoList(commits: List[CommitInfoRecord]): List[CommitInfo] = {
      commits.map(toCommitInfo(_))
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
}

object CommitInfoRecord extends CommitInfoRecord with MongoMetaRecord[CommitInfoRecord] {
  override def collectionName = "commit_infos"

  def ensureIndexes() {
    this.ensureIndex(keys = (committerDate.name -> 1) ~ (authorDate.name -> 1))
  }
}

