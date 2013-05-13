package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.Mongo
import com.typesafe.scalalogging.slf4j.Logging

object MongoInit extends Logging {

  def ensureIndexes() {
    logger.info("Ensuring Mongo indexes")
    CommitInfoRecord.ensureIndexes()
  }

  def initialize() {
    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo, "codebrag")
    ensureIndexes()
  }
}
