package com.softwaremill.codebrag.dao.reporting

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{DateField, ObjectIdPk}
import com.softwaremill.codebrag.dao.{CommitInfoRecord, LongStringField}
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime
import java.util.Date

class MongoCommitListFinder extends CommitListFinder {

  def recordToDto(recordData: (String, String, String, String, Date)): CommitListItemDTO = {
    CommitListItemDTO(recordData._1, recordData._2, recordData._3,
      recordData._4, recordData._5)
  }

  override def findAllPendingCommits() = {
    val records = CommitInfoRecord.select(_.sha, _.message, _.authorName, _.committerName, _.date).orderDesc(_.date).fetch()
    CommitListDTO(records.map(recordToDto(_)))
  }
}