package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.{CommitReviewTaskRecord, CommitInfoRecord}
import com.foursquare.rogue.LiftRogue._
import java.util.Date
import org.bson.types.ObjectId

class MongoCommitListFinder extends CommitListFinder {

  override def findCommitsToReviewForUser(userId: ObjectId) = {
    val userReviewTasks = CommitReviewTaskRecord.where(_.userId eqs userId).fetch()
    val commitIds = userReviewTasks.map(_.commitId.get).toSet
    val commits = CommitInfoRecord.select(_.id, _.sha, _.message, _.authorName, _.committerName, _.date).where(_.id in commitIds).orderDesc(_.date).fetch()
    CommitListDTO(commits.map(recordToDto(_)))
  }

  private def recordToDto(recordData: (ObjectId, String, String, String, String, Date)): CommitListItemDTO = {
    CommitListItemDTO(recordData._1.toString, recordData._2, recordData._3,
      recordData._4, recordData._5, recordData._6)
  }

}