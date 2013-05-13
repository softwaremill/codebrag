package com.softwaremill.codebrag.dao

import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.Mongo

object MongoInit {

  def initialize() {
    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo, "codebrag")
  }
}
