package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId
import com.softwaremill.codebrag.dao.{CommentRecord, UserRecord, CommitReviewRecord}
import com.foursquare.rogue.LiftRogue._

class MongoCommentListFinder extends CommentListFinder {


  override def findAllForCommit(commitId: ObjectId) = {
    val recordOption = CommitReviewRecord.where(_.id eqs commitId).get()
    CommentListDTO(
      recordOption match {
        case None => List.empty
        case Some(record) => buildCommentsFromRecord(record)
      })
  }

  private def buildCommentsFromRecord(record: CommitReviewRecord): List[CommentListItemDTO] = {

    def buildCommentItem(record: CommentRecord, namesGroupedById: Map[ObjectId, String]): CommentListItemDTO = {
      val userName = namesGroupedById.getOrElse(record.authorId.get, "Unknown user")
      CommentListItemDTO(record.id.get.toString, userName, record.message.get, record.date.get)
    }

    val comments = record.comments.get.sortBy(_.date.get)
    val authorIdList = comments.map(_.authorId.get)
    val idNamesPairs = UserRecord.select(_.id, _.name).where(_.id in authorIdList).fetch
    val namesGroupedById = idNamesPairs.toMap
    comments.map(buildCommentItem(_, namesGroupedById))
  }
}
