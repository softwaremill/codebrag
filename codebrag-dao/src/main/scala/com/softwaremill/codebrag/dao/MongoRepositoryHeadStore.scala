package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.json.JsonDSL._
import com.foursquare.rogue.LiftRogue._
import com.typesafe.scalalogging.slf4j.Logging
import net.liftweb.record.field.BooleanField
import com.softwaremill.codebrag.domain.RepositoryStatus

trait RepositoryHeadStore {
  def update(repoName: String, newSha: String)
  def get(repoName: String): Option[String]

  def updateRepoStatus(newStatus: RepositoryStatus)
  def getRepoStatus(repoName: String): Option[RepositoryStatus]
}

class MongoRepositoryHeadStore extends RepositoryHeadStore with Logging {

  def updateRepoStatus(newStatus: RepositoryStatus) {
    RepositoryHeadRecord.where(_.repoName eqs newStatus.repositoryName)
      .modifyOpt(newStatus.headId)(_.sha setTo _)
      .modify(_.repoReady setTo newStatus.ready)
      .modify(_.repoStatusError setTo newStatus.error)
      .upsertOne()
  }

  def getRepoStatus(repoName: String) = {
    RepositoryHeadRecord.where(_.repoName eqs repoName).get() match {
      case Some(record) => Some(RepositoryStatus(repoName, Option(record.sha.get), record.repoReady.get, record.repoStatusError.get))
      case None => None
    }
  }

  def update(repoName: String, newSha: String) {
      updateRepoStatus(RepositoryStatus.ready(repoName).withHeadId(newSha))
  }

  def get(repoName: String) = {
    RepositoryHeadRecord.where(_.repoName eqs repoName).get() match {
      case Some(record) => Some(record.sha.get)
      case None => None
    }
  }

}

class RepositoryHeadRecord extends MongoRecord[RepositoryHeadRecord] with ObjectIdPk[RepositoryHeadRecord] {

  def meta = RepositoryHeadRecord

  object sha extends LongStringField(this)

  object repoName extends LongStringField(this)

  object repoReady extends BooleanField(this)

  object repoStatusError extends OptionalLongStringField(this)

}

object RepositoryHeadRecord extends RepositoryHeadRecord with MongoMetaRecord[RepositoryHeadRecord] {
  override def collectionName = "repo_head"

  def ensureIndexes() {
    this.ensureIndex(keys = (repoName.name -> 1), unique = true)
  }
}

