package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.{CommitComment, CommitInfo}
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{BsonRecordListField, MongoListField, DateField, ObjectIdPk}
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime
import org.bson.types.ObjectId

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

  def findAll(): List[CommitInfo] = {
    CommitInfoRecord.orderDesc(_.date).fetch()
  }

  private object CommitInfoImplicits {

    implicit def toCommitInfo(record: CommitInfoRecord): CommitInfo = {
      val comments = record.comments;
      CommitInfo(record.id.get, record.sha.get, record.message.get, record.authorName.get, record.committerName.get, new DateTime(record.date.get), record.parents.get,
      comments.get)
    }

    implicit def toCommentRecord(comment: CommitComment) = {
      CommentRecord.createRecord
      .id(comment.id.toString)
      .authorName(comment.commentAuthorName)
      .message(comment.message)
      .date(comment.postingTime.toDate)
    }

    implicit def toCommentRecords(comments: List[CommitComment]) = {
      comments.map(toCommentRecord(_))
    }

    implicit def toCommitInfo(record: Option[CommitInfoRecord]): Option[CommitInfo] = {
      record.map(toCommitInfo(_))
    }

    implicit def commentRecordsToCommentList(records: List[CommentRecord]): List[CommitComment] = {
      records.map(commentRecordToComment(_))
    }

      implicit def commentRecordToComment(record: CommentRecord): CommitComment = {
      CommitComment(new ObjectId(record.id.get), record.authorName.get, record.message.get, new DateTime(record.date.get))
    }

    implicit def toCommitInfoRecord(commit: CommitInfo): CommitInfoRecord = {
      CommitInfoRecord.createRecord
        .id(commit.id)
        .sha(commit.sha)
        .message(commit.message)
        .authorName(commit.authorName)
        .committerName(commit.committerName)
        .date(commit.date.toDate)
        .parents(commit.parents)
        .comments(commit.comments)
    }

    implicit def toCommitInfoRecordList(commits: List[CommitInfo]): List[CommitInfoRecord] = {
      commits.map(toCommitInfoRecord(_))
    }
    implicit def toCommitInfoList(commits: List[CommitInfoRecord]): List[CommitInfo] = {
      commits.map(toCommitInfo(_))
    }

  }

}

class CommentRecord extends BsonRecord[CommentRecord] {
  def meta = CommentRecord

  object id extends LongStringField(this)
  object authorName extends LongStringField(this)
  object message extends LongStringField(this)
  object date extends DateField(this)
}

object CommentRecord extends CommentRecord with BsonMetaRecord[CommentRecord] {
}


class CommitInfoRecord extends MongoRecord[CommitInfoRecord] with ObjectIdPk[CommitInfoRecord] {
  def meta = CommitInfoRecord

  object sha extends LongStringField(this)

  object message extends LongStringField(this)

  object authorName extends LongStringField(this)

  object committerName extends LongStringField(this)

  object date extends DateField(this)

  object parents extends MongoListField[CommitInfoRecord, String](this)

  object comments extends BsonRecordListField(this, CommentRecord)
}

object CommitInfoRecord extends CommitInfoRecord with MongoMetaRecord[CommitInfoRecord] {
  override def collectionName: String = "commitInfos"
}
