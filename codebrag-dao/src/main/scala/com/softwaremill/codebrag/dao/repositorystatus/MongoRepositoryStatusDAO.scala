package com.softwaremill.codebrag.dao.repositorystatus

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.json.JsonDSL._
import com.foursquare.rogue.LiftRogue._
import com.typesafe.scalalogging.slf4j.Logging
import net.liftweb.record.field.BooleanField
import com.softwaremill.codebrag.domain.RepositoryStatus
import com.softwaremill.codebrag.dao.mongo.{OptionalLongStringField, LongStringField}

class MongoRepositoryStatusDAO extends RepositoryStatusDAO with Logging {

  def updateRepoStatus(newStatus: RepositoryStatus) {
    RepositoryStatusRecord.where(_.repoName eqs newStatus.repositoryName)
      .modify(_.repoReady setTo newStatus.ready)
      .modify(_.repoStatusError setTo newStatus.error)
      .upsertOne()
  }

  def getRepoStatus(repoName: String) = {
    RepositoryStatusRecord.where(_.repoName eqs repoName).get() match {
      case Some(record) => Some(RepositoryStatus(repoName, record.repoReady.get, record.repoStatusError.get))
      case None => None
    }
  }

}

class RepositoryStatusRecord extends MongoRecord[RepositoryStatusRecord] with ObjectIdPk[RepositoryStatusRecord] {

  def meta = RepositoryStatusRecord

  object repoName extends LongStringField(this)

  object repoReady extends BooleanField(this)

  object repoStatusError extends OptionalLongStringField(this)

}

object RepositoryStatusRecord extends RepositoryStatusRecord with MongoMetaRecord[RepositoryStatusRecord] {
  override def collectionName = "repo_head"

  def ensureIndexes() {
    this.ensureIndex(keys = (repoName.name -> 1), unique = true)
  }
}

