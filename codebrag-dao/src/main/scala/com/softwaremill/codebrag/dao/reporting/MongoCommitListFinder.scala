package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.{CommitReviewTaskRecord, CommitInfoRecord}
import com.foursquare.rogue.LiftRogue._
import java.util.Date
import org.bson.types.ObjectId
import com.foursquare.rogue.Query

class MongoCommitListFinder extends CommitListFinder {

  override def findCommitsToReviewForUser(userId: ObjectId) = {
    val userReviewTasks = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
    val commitIds = userReviewTasks.map(_.commitId.get).toSet
    val commits = projectionQuery.where(_.id in commitIds).orderDesc(_.date).fetch()
    CommitListDTO(commits.map(recordToDto(_)))
  }

  override def findCommitInfoById(commitIdStr: String) = {
    val commitId = new ObjectId(commitIdStr)
    val commitInfoOption = CommitInfoRecord.select(_.id, _.sha, _.message, _.authorName, _.committerName, _.date).where(_.id eqs commitId).get()
    commitInfoOption match {
      case Some(record) => Right(recordToDto(record))
      case None => Left(s"No such commit $commitIdStr")
    }
  }

  private def recordToDto(recordData: (ObjectId, String, String, String, String, Date)): CommitListItemDTO = {
    CommitListItemDTO(recordData._1.toString, recordData._2, recordData._3,
      recordData._4, recordData._5, recordData._6)
  }

  private def projectionQuery = {
    CommitInfoRecord.select(_.id, _.sha, _.message, _.authorName, _.committerName, _.date)
  }

}