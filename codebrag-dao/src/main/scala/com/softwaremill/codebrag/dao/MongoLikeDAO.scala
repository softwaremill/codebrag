package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.Like
import org.bson.types.ObjectId
import org.joda.time.DateTime
import com.foursquare.rogue.LiftRogue._

class MongoLikeDAO extends LikeDAO {

  override def save(like: Like) {
    LikeToRecordBuilder.buildFrom(like).save
  }

  override def findById(likeId: ObjectId) = {
    LikeRecord.where(_.id eqs likeId).get().map(RecordToLikeBuilder.buildFrom(_))
  }

  override def findLikesForCommit(commitId: ObjectId) = {
    val likes = LikeRecord.where(_.commitId eqs commitId).orderAsc(_.date).fetch()
    likes.map(RecordToLikeBuilder.buildFrom(_))
  }

  def findAllLikesInThreadWith(like: Like) = {
    val query = (like.fileName, like.lineNumber) match {
      case (Some(fileName), Some(lineNumber)) => {
        LikeRecord.where(_.commitId eqs like.commitId).and(_.fileName eqs fileName).and(_.lineNumber eqs lineNumber)
      }
      case _ => {
        LikeRecord.where(_.commitId eqs like.commitId).and(_.fileName exists false).and(_.lineNumber exists false)
      }
    }
    query.fetch().map(RecordToLikeBuilder.buildFrom(_))
  }

  private object LikeToRecordBuilder {

    def buildFrom(like: Like) = {
      LikeRecord.createRecord
        .id(like.id)
        .commitId(like.commitId)
        .authorId(like.authorId)
        .date(like.postingTime.toDate)
        .fileName(like.fileName)
        .lineNumber(like.lineNumber)
    }

  }

  private object RecordToLikeBuilder {

    def buildFrom(record: LikeRecord) = {
      Like(
        record.id.get,
        record.commitId.get,
        record.authorId.get,
        new DateTime(record.date.get),
        record.fileName.get,
        record.lineNumber.get)
    }

  }
}