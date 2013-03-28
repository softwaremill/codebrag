package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.CommitInfoRecord
import com.foursquare.rogue.LiftRogue._
import java.util.Date
import org.bson.types.ObjectId

class MongoCommitListFinder extends CommitListFinder {

  def recordToDto(recordData: (ObjectId, String, String, String, String, Date)): CommitListItemDTO = {
    CommitListItemDTO(recordData._1.toString, recordData._2, recordData._3,
      recordData._4, recordData._5, recordData._6)
  }

  override def findAllPendingCommits() = {
    val records = CommitInfoRecord.select(_.id, _.sha, _.message, _.authorName, _.committerName, _.date).orderDesc(_.date).fetch()
    CommitListDTO(records.map(recordToDto(_)))
  }
}