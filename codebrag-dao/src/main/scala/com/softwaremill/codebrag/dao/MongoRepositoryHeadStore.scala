package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.json.JsonDSL._
import com.foursquare.rogue.LiftRogue._
import com.mongodb.DBObject
import com.typesafe.scalalogging.slf4j.Logging

trait RepositoryHeadStore {
  def update(repoName: String, newSha: String)
  def get(repoName: String): Option[String]
}

class MongoRepositoryHeadStore extends RepositoryHeadStore with Logging {

  def update(repoName: String, newSha: String) {
    val query = RepositoryHeadRecord.where(_.repoName eqs repoName)
    val updated = createUpdatedVersion(newSha, repoName)
    RepositoryHeadRecord.upsert(query.asDBObject, updated)
    logger.debug(s"Saving $newSha as a HEAD of $repoName repo")
  }


  def createUpdatedVersion(newSha: String, repoName: String): DBObject = {
    val updated = RepositoryHeadRecord.createRecord
      .sha(newSha)
      .repoName(repoName)
      .asDBObject
    updated.removeField("_id")
    updated
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

}

object RepositoryHeadRecord extends RepositoryHeadRecord with MongoMetaRecord[RepositoryHeadRecord] {
  override def collectionName = "repo_head"

  def ensureIndexes() {
    this.ensureIndex(keys = (repoName.name -> 1), unique = true)
  }
}

